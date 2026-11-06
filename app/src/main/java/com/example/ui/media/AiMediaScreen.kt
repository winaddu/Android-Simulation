package com.example.ui.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMediaScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var prompt by remember { mutableStateOf("") }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var analysisResult by remember { mutableStateOf<String?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(inputStream)
                selectedImageBitmap = bmp
                analysisResult = null
            } catch (e: Exception) {
                Log.e("AiMedia", "Failed to load image", e)
            }
        }
    }

    fun analyzeImage() {
        val bmp = selectedImageBitmap ?: return
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val model = Firebase.ai.generativeModel("gemini-4-argon")
                val response = withTimeout(60_000L) {
                    model.generateContent("Analyze this uploaded image and provide detailed insights.")
                }
                analysisResult = response.text ?: "No analysis returned."
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Image analysis failed."
            } finally {
                isLoading = false
            }
        }
    }

    fun generateImage() {
        val p = prompt.trim()
        if (p.isEmpty()) return
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val model = Firebase.ai.generativeModel("gemini-nano-banana-2.1")
                val response = withTimeout(60_000L) {
                    model.generateContent("Generate image description with aspect ratio $selectedAspectRatio: $p")
                }
                errorMessage = "Image generation requested for '$p' ($selectedAspectRatio)."
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Image generation failed."
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎨 AI Media & Vision Studio") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("AI Image Generation (Nano Banana 2.1)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = prompt,
                            onValueChange = { prompt = it },
                            placeholder = { Text("Describe image to generate...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Aspect Ratio:", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("1:1", "4:3", "16:9", "9:16").forEach { ratio ->
                                FilterChip(
                                    selected = selectedAspectRatio == ratio,
                                    onClick = { selectedAspectRatio = ratio },
                                    label = { Text(ratio) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { generateImage() },
                            enabled = !isLoading && prompt.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Generate Image")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Image Understanding (Gemini 4 Argon)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (selectedImageBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Image(
                                    bitmap = selectedImageBitmap!!.asImageBitmap(),
                                    contentDescription = "Selected Image",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Pick Image")
                            }

                            Button(
                                onClick = { analyzeImage() },
                                enabled = !isLoading && selectedImageBitmap != null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Analyze Image")
                            }
                        }

                        if (analysisResult != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = analysisResult!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
