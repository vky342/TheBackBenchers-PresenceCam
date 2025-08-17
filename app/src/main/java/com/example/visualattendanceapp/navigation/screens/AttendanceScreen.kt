package com.example.visualattendanceapp.navigation.screens



import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.visualattendanceapp.data.RetrofitInstance
import com.example.visualattendanceapp.data.uriToMultipart
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

data class Participant(
    val enrollNo: String,
    val name: String,
    val photos: List<Uri?>
)

data class RegisterResponse(
    val enrollNo: String
)


@Preview
@Composable
fun RegisterScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var enrollNo by remember { mutableStateOf("") }
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var apiMessage by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Gallery picker for multiple images
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            selectedUris = uris.take(3) // only take first 3 if more selected
        }
    )

    // --------- CAMERA LAUNCHER ---------
    var currentImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentImageUri != null) {
            if (selectedUris.size < 3) {
                selectedUris = selectedUris + currentImageUri!!
            }
        }
    }

    fun captureImage() {
        if (selectedUris.size < 3) {
            val file = File.createTempFile(
                "image_${System.currentTimeMillis()}",
                ".jpg",
                context.cacheDir
            )
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            currentImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(24, 23, 23))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(" ⎣⌆⎦ Student", textAlign = TextAlign.Left, modifier = Modifier.fillMaxWidth(),color = Color(238, 238, 238),style = MaterialTheme.typography.headlineLarge)
        Text(" Registration",textAlign = TextAlign.Left, modifier = Modifier.fillMaxWidth(),color = Color(238, 238, 238),style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(35.dp))

        OutlinedTextField(
            value = enrollNo,
            onValueChange = { enrollNo = it },
            label = { Text("Enroll No") },
            textStyle = TextStyle(fontSize = 18.sp, color = Color.White),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(24, 23, 23),
                unfocusedContainerColor = Color(24, 23, 23),

                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,

                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray,

                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.Gray
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus() // Unfocus on Done
                }
            )
        )

        Spacer(modifier = Modifier.height(35.dp))

        // Upload Images Button

        // ---------- IMAGE BUTTONS ----------
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
            // Upload from gallery
            Button(
                modifier = Modifier
                    .height(50.dp)
                    .wrapContentWidth()
                    .shadow(shape = RoundedCornerShape(25), elevation = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(70, 138, 154)),
                shape = RoundedCornerShape(25),
                onClick = {
                    galleryLauncher.launch(arrayOf("image/*"))
                }
            ) {
                Text("⎜▲⎟  Upload", style = MaterialTheme.typography.titleMedium, color = Color.Black)
            }

            // Capture from camera
            Button(
                modifier = Modifier
                    .height(50.dp)
                    .wrapContentWidth()
                    .shadow(shape = RoundedCornerShape(25), elevation = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(138, 154, 70)),
                shape = RoundedCornerShape(25),
                onClick = { captureImage() }
            ) {
                Text("⎜⏣⎟  Capture", style = MaterialTheme.typography.titleMedium, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(35.dp))

        Text(" ▶︎ note ◀︎ ", style = MaterialTheme.typography.titleLarge, color = Color(
            138,
            36,
            36,
            255
        )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("please upload three clear close-up photos of your face—one front-facing and two from each side—taken in good lighting so your features are clearly visible.",
            textAlign = TextAlign.Center,modifier = Modifier.fillMaxWidth(0.8f),style = MaterialTheme.typography.bodyLarge.copy(
            fontStyle = FontStyle.Italic
        ), color = Color(89, 88, 88, 255))

        Spacer(modifier = Modifier.height(35.dp))

        // Show selected images
        if (selectedUris.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(selectedUris) { uri ->
                    Card(
                        modifier = Modifier
                            .size(100.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Register Button
        Button(modifier = Modifier.height(50.dp).wrapContentWidth().shadow(shape = RoundedCornerShape(25), elevation = 4.dp),
            shape = RoundedCornerShape(25),
            onClick = {
                coroutineScope.launch {
                    if (enrollNo.isBlank() || selectedUris.size != 3) {
                        apiMessage = "Enter enroll no & select 3 images"
                        return@launch
                    }

                    isLoading = true

                    try {
                        val enrollPart = enrollNo.toRequestBody("text/plain".toMediaTypeOrNull())
                        val imageParts = selectedUris.map { uri ->
                            uriToMultipart(context, uri, "images")
                        }

                        val response = RetrofitInstance.api.register(imageParts, enrollPart)

                        if (response.isSuccessful) {
                            response.body()?.let { body ->
                                apiMessage = "${body.message} | Total students: ${body.total_students}"
                                // Reset form
                                enrollNo = ""
                                selectedUris = emptyList()
                            }
                        } else {
                            apiMessage = "API Error: ${response.errorBody()?.string()}"
                        }

                    } catch (e: Exception) {
                        apiMessage = "Exception: ${e.localizedMessage}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = selectedUris.size == 3 && enrollNo.isNotBlank() && !isLoading,
            colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(30, 28, 28, 255),
            containerColor = if (selectedUris.size == 3) Color(70, 138, 154)
            else Color(84, 18, 18)
        )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .size(20.dp)
                )
            } else {
                Text("Register",style = MaterialTheme.typography.titleMedium, color = Color(
                    0,
                    0,
                    0,
                    255
                )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show API message
        apiMessage?.let {
            Text(it,  style = MaterialTheme.typography.titleLarge, color = Color(89, 88, 88, 255)
            )
        }
    }
}