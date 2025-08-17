package com.example.visualattendanceapp.navigation.screens


import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.visualattendanceapp.data.RetrofitInstance
import com.example.visualattendanceapp.data.uriToMultipart
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

data class Participant(
    val enrollNo: String,
    val name: String,
    val photos: List<Uri?>
)

data class RegisterResponse(
    val enrollNo: String
)
@Composable
fun RegisterScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var enrollNo by remember { mutableStateOf("") }
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var apiMessage by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }

    // Gallery picker for multiple images
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            selectedUris = uris.take(3) // only take first 3 if more selected
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📝 Register Student", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = enrollNo,
            onValueChange = { enrollNo = it },
            label = { Text("Enroll No") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Upload Images Button
        Button(onClick = {
            galleryLauncher.launch(arrayOf("image/*"))
        }) {
            Text("📂 Upload 3 Images")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Show selected images
        if (selectedUris.isNotEmpty()) {
            LazyRow {
                items(selectedUris) { uri ->
                    Text("✔ ${uri.lastPathSegment}", modifier = Modifier.padding(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Register Button
        Button(
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
            enabled = selectedUris.size == 3 && enrollNo.isNotBlank() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .size(20.dp)
                )
            } else {
                Text("✅ Register")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show API message
        apiMessage?.let {
            Text(it, style = MaterialTheme.typography.bodyLarge)
        }
    }
}