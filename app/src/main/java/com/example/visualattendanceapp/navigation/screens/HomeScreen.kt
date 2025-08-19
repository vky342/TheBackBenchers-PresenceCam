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
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Card
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Size
import com.example.visualattendanceapp.data.RecognizeResponse
import com.example.visualattendanceapp.data.RetrofitInstance
import com.example.visualattendanceapp.data.uriToMultipart
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.file.WatchEvent


// API Response Model
data class ApiResponse(
    val recognized: List<String>, // list of enrollNo
    val unrecognizedFaces: String, // base64 image string
    val allFaces: String           // base64 image string
)

@Preview
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var recognizedList by remember { mutableStateOf(listOf<String>()) }
    var apiResponse by remember { mutableStateOf<RecognizeResponse?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    var selectedImage by remember { mutableStateOf<ByteArray?>(null) }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(24, 23, 23))
    ) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(24, 23, 23))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            " ⎧●⎫ Visual",
            textAlign = TextAlign.Left,
            modifier = Modifier.fillMaxWidth(),
            color = Color(238, 238, 238),
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            " Attendance",
            textAlign = TextAlign.Left,
            modifier = Modifier.fillMaxWidth(),
            color = Color(238, 238, 238),
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(35.dp))

        // Pick/Take Photo Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier.height(50.dp).wrapContentWidth()
                    .shadow(shape = RoundedCornerShape(25), elevation = 4.dp),
                colors = ButtonDefaults.buttonColors().copy(containerColor = Color(70, 138, 154)),
                shape = RoundedCornerShape(25),
                onClick = { galleryLauncher.launch(arrayOf("image/*")) }) {
                Text(
                    "⎜▲⎟  Upload", style = MaterialTheme.typography.titleMedium, color = Color(
                        0,
                        0,
                        0,
                        255
                    )
                )
            }
            Button(
                modifier = Modifier.height(50.dp).wrapContentWidth()
                    .shadow(shape = RoundedCornerShape(25), elevation = 4.dp),
                colors = ButtonDefaults.buttonColors().copy(containerColor = Color(138, 154, 70)),
                shape = RoundedCornerShape(25),
                onClick = {
                    cameraLauncher.launch()
                }) {
                Text(
                    "⎜⏣⎟  Capture", style = MaterialTheme.typography.titleMedium, color = Color(
                        0,
                        0,
                        0,
                        255
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(35.dp))

        // Send / Reset Button
        Button(
            modifier = Modifier.height(50.dp).wrapContentWidth()
                .shadow(shape = RoundedCornerShape(25), elevation = 4.dp),
            shape = RoundedCornerShape(25),
            onClick = {
                if (apiResponse == null) {
                    // SEND
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            selectedPhotoUri?.let { uri ->

                                Log.d(
                                    "SEND",
                                    "Code: 584 started to convert to Multipart",
                                )

                                val imagePart = uriToMultipart(context, uri, "file")
                                Log.d(
                                    "SEND",
                                    "Code: 585 ending to convert to Multipart",
                                )
                                Log.d(
                                    "SEND",
                                    "Code: 586 starting to call the api",
                                )

                                val response = RetrofitInstance.api.recognize(imagePart)

                                Log.d(
                                    "API",
                                    "Response: 587 received",
                                )

                                Log.d(
                                    "API",
                                    "Code: ${response.code()}, Message: ${response.message()}"
                                )

                                if (response.isSuccessful) {

                                    val body = response.body()
                                    apiResponse = body

                                    Log.d("API", response.body()?.recognized_ids.toString())
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
                            Toast.makeText(context, "Exception: ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        } finally {
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
                disabledContainerColor = Color(30, 28, 28, 255),
                containerColor = if (apiResponse == null) Color(70, 138, 154)
                else Color(84, 18, 18)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    if (apiResponse == null) "⎜▶︎⎜ Send" else " ✖︎ Reset",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(
                        0,
                        0,
                        0,
                        255
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Recognized list
        Text(
            "Recognized Enrollments",
            color = Color(238, 238, 238),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(28.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(50.dp, 500.dp)
        ) {
            items(recognizedList) { enrollNo ->
                Text(
                    "- $enrollNo",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(238, 238, 238),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // image show
        Spacer(modifier = Modifier.height(16.dp))

        // Add manually
        Button(
            modifier = Modifier.height(50.dp).wrapContentWidth()
                .shadow(shape = RoundedCornerShape(25), elevation = 5.dp),
            colors = ButtonDefaults.buttonColors().copy(containerColor = Color(70, 138, 154)),
            shape = RoundedCornerShape(25),
            onClick = { showAddDialog = true }) {
            Text(
                " +  Add Manually", style = MaterialTheme.typography.titleMedium, color = Color(
                    0,
                    0,
                    0,
                    255
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save PDF
        Button(
            modifier = Modifier.height(50.dp).wrapContentWidth()
                .shadow(shape = RoundedCornerShape(25), elevation = 4.dp),
            colors = ButtonDefaults.buttonColors().copy(containerColor = Color(138, 154, 70)),
            shape = RoundedCornerShape(25),
            onClick = {
                createDocumentLauncher.launch("attendance_list.pdf")
            }) {
            Text(
                "❖ Save as PDF", style = MaterialTheme.typography.titleMedium, color = Color(
                    0,
                    0,
                    0,
                    255
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        apiResponse?.let { response ->
            Text(
                "Detected Faces:",
                style = MaterialTheme.typography.titleSmall,
                color = Color(238, 238, 238)
            )

            val imagesBase64 = listOfNotNull(
                response.annotated_all.takeIf { it.isNotBlank() },
                response.annotated_unrecognized.takeIf { it.isNotBlank() }
            )

            if (imagesBase64.isNotEmpty()) {
                val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                val density = LocalDensity.current
                val screenWidthPx = with(density) { screenWidth.toPx().toInt() }

                // Precompute heights for each image
                val imagesHeights = imagesBase64.map { base64Str ->
                    val imageBytes =
                        android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
                    screenWidth * aspectRatio
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imagesHeights.fold(0.dp) { acc, dp -> acc + dp })  // sum safely
                ) {
                    itemsIndexed(imagesBase64) { _, base64Str ->
                        // Decode base64 only once into ByteArray
                        val imageBytes = remember(base64Str) {
                            android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
                        }

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageBytes) // give Coil the byte array
                                .size(Size.ORIGINAL) // let Coil manage scaling
                                .build(),
                            contentDescription = "Recognized Face",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { selectedImage = imageBytes }, // open fullscreen later
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
        selectedImage?.let { imageBytes ->
            val bitmap = remember(imageBytes) {
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            }

            val scale = remember { mutableStateOf(1f) }
            val offset = remember { mutableStateOf(Offset.Zero) }
            val state = rememberTransformableState { zoomChange, panChange, _ ->
                scale.value *= zoomChange
                offset.value += panChange
            }

            BackHandler { selectedImage = null }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { selectedImage = null },
                contentAlignment = Alignment.Center
            ) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Fullscreen Image",
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale.value
                                scaleY = scale.value
                                translationX = offset.value.x
                                translationY = offset.value.y
                            }
                            .transformable(state)
                            .fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

    }
}


fun decodeBase64Image(base64Str: String, reqWidth: Int, reqHeight: Int): Bitmap {
    val imageBytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)

    // First decode with inJustDecodeBounds=true to check dimensions
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)

    // Calculate sample size
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

