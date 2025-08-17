package com.example.visualattendanceapp.navigation.screens


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.visualattendanceapp.data.RecognizeResponse
import com.example.visualattendanceapp.data.RetrofitInstance
import com.example.visualattendanceapp.data.uriToMultipart
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


// API Response Model
data class ApiResponse(
    val recognized: List<String>, // list of enrollNo
    val unrecognizedFaces: String, // base64 image string
    val allFaces: String           // base64 image string
)

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var recognizedList by remember { mutableStateOf(listOf<String>()) }
    var apiResponse by remember { mutableStateOf<RecognizeResponse?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // Launcher for gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                selectedPhotoUri = uri
            }
        }
    )

// Launcher for camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            bitmap?.let {
                val file = File(context.cacheDir, "camera_photo.jpg")
                file.outputStream().use { out ->
                    it.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                selectedPhotoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            }
        }
    )

    // Launcher to create document
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        uri?.let {
            try {
                val pdfDocument = android.graphics.pdf.PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
                val page = pdfDocument.startPage(pageInfo)

                val canvas = page.canvas
                val paint = Paint()
                paint.textSize = 14f

                var y = 50f
                canvas.drawText("Attendance List", 50f, y, paint)
                y += 30f

                recognizedList.forEach { name ->
                    canvas.drawText(name, 50f, y, paint)
                    y += 25f
                }

                pdfDocument.finishPage(page)

                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }

                pdfDocument.close()
                Toast.makeText(context, "PDF saved successfully!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📸 Visual Attendance", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        // Pick/Take Photo Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { galleryLauncher.launch(arrayOf("image/*")) }) {
                Text("📂 Upload")
            }
            Button(onClick = {
                cameraLauncher.launch()
            }) {
                Text("📷 Camera")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Send / Reset Button
        Button(
            onClick = {
                if (apiResponse == null) {
                    // SEND
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            selectedPhotoUri?.let { uri ->
                                val imagePart = uriToMultipart(context, uri, "file")
                                val response = RetrofitInstance.api.recognize(imagePart)

                                Log.d("API", "Code: ${response.code()}, Message: ${response.message()}")

                                if (response.isSuccessful) {

                                    val body = response.body()
                                    apiResponse = body

                                    Log.d("API",response.body()?.recognized_ids.toString())
                                    val recognizedIds = body?.recognized_ids

                                    recognizedList = if (recognizedIds.isNullOrEmpty()) {
                                        listOf("No Faces Recognised")
                                    } else {
                                        recognizedIds.toList()
                                    }

                                } else {
                                    Toast.makeText(context, "API Error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                        }finally {
                            isLoading = false // stop loading
                        }
                    }
                } else {
                    // RESET
                    apiResponse = null
                    recognizedList = emptyList()
                    selectedPhotoUri = null
                }
            },
            enabled = selectedPhotoUri != null && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (apiResponse == null) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(if (apiResponse == null) "🚀 Send" else "🔄 Reset")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recognized list
        Text("✅ Recognized Enrollments:", style = MaterialTheme.typography.titleMedium)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(recognizedList) { enrollNo ->
                Text("- $enrollNo", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,style = MaterialTheme.typography.bodyLarge)
            }
        }

        // image show
        Spacer(modifier = Modifier.height(16.dp))

        // Add manually
        Button(onClick = { showAddDialog = true }) {
            Text("➕ Add Enroll No Manually")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save PDF
        Button(onClick = {
            createDocumentLauncher.launch("attendance_list.pdf")
        }) {
            Text("💾 Save as PDF")
        }

        Spacer(modifier = Modifier.height(16.dp))

        apiResponse?.let { response ->
            Text("Detected Faces:", style = MaterialTheme.typography.titleMedium)

            val imagesBase64 = listOfNotNull(
                response.annotated_all.takeIf { it.isNotBlank() },
                response.annotated_unrecognized.takeIf { it.isNotBlank() }
            )

            if (imagesBase64.isNotEmpty()) {
                val screenWidth = LocalConfiguration.current.screenWidthDp.dp

                // Precompute heights for each image
                val imagesHeights = imagesBase64.map { base64Str ->
                    val imageBytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
                    screenWidth * aspectRatio
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imagesHeights.fold(0.dp) { acc, dp -> acc + dp })  // sum safely
                ) {
                    itemsIndexed(imagesBase64) { index, base64Str ->
                        val imageBytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Recognized Face",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(imagesHeights[index])
                                .padding(8.dp),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
            } else {
                Text(
                    "No recognized images yet",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

    // Manual Add Dialog
    if (showAddDialog) {
        var enrollInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Enroll No") },
            text = {
                OutlinedTextField(
                    value = enrollInput,
                    onValueChange = { enrollInput = it },
                    label = { Text("Enroll No") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (enrollInput.isNotBlank()) {
                        recognizedList = recognizedList + enrollInput
                    }
                    showAddDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                Button(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Writes a simple PDF to the given URI chosen by the user
 */
fun savePdfToUri(context: Context, uri: Uri, students: List<String>) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas
        val paint = android.graphics.Paint()
        paint.textSize = 16f

        var y = 50f
        canvas.drawText("Attendance List", 50f, y, paint)

        students.forEachIndexed { index, student ->
            y += 30f
            canvas.drawText("${index + 1}. $student", 50f, y, paint)
        }

        pdfDocument.finishPage(page)

        context.contentResolver.openOutputStream(uri)?.use { output ->
            pdfDocument.writeTo(output)
        }

        pdfDocument.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
}
