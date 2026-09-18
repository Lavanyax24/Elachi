package com.elachi.app.ui.recipe

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

/**
 * FR-2.2 (Camera OCR) and, when launched with a gallery Uri instead of a
 * live camera frame, the basis for FR-2.3 (Screenshot import).
 *
 * Structuring the recognised text is delegated to [CameraCaptureViewModel]:
 * it tries the AI-powered backend parser first (Cohere), and only falls
 * back to the on-device regex parser if that call fails. This screen's own
 * job is just "get raw text off the image" via ML Kit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraCaptureScreen(
    viewModel: CameraCaptureViewModel,
    onTextRecognized: (title: String, ingredients: List<DraftIngredient>, steps: List<String>, method: String, servings: String, cookTimeMinutes: String) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var isProcessing by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    fun deliver(result: OcrResult) =
        onTextRecognized(result.title, result.ingredients, result.steps, result.method, result.servings, result.cookTimeMinutes)

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            isProcessing = true
            scope.launch {
                try {
                    val image = InputImage.fromFilePath(context, uri)
                    val rawLines = recognizeRawText(image)
                    viewModel.processRecognizedText(rawLines) { result ->
                        isProcessing = false
                        deliver(result)
                    }
                } catch (e: Exception) {
                    isProcessing = false
                    errorText = "Couldn't read that image. Try a clearer photo or enter the recipe manually."
                }
            }
        }
    }

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Capture Recipe") }) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (hasCameraPermission) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val capture = ImageCapture.Builder().build()
                                imageCapture = capture
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture,
                                    )
                                } catch (e: Exception) {
                                    errorText = "Couldn't start the camera on this device."
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                    )

                    if (isProcessing) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(8.dp))
                                Text("Reading your recipe...", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Camera permission is required to capture a recipe.")
                }
            }

            errorText?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
                OutlinedButton(
                    onClick = { galleryLauncher.launch(androidx.activity.result.PickVisualMediaRequest()) },
                    modifier = Modifier.weight(1f),
                ) { Text("Choose from Gallery") }
                Button(
                    onClick = {
                        val capture = imageCapture ?: return@Button
                        isProcessing = true
                        captureRawText(
                            capture = capture,
                            onResult = { rawLines ->
                                viewModel.processRecognizedText(rawLines) { result ->
                                    isProcessing = false
                                    deliver(result)
                                }
                            },
                            onFailure = {
                                isProcessing = false
                                errorText = "Couldn't recognise text in that photo. Try again with better lighting."
                            },
                        )
                    },
                    enabled = hasCameraPermission && !isProcessing,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Capture")
                }
            }
        }
    }
}

private fun captureRawText(
    capture: ImageCapture,
    onResult: (List<String>) -> Unit,
    onFailure: () -> Unit,
) {
    val executor = java.util.concurrent.Executors.newSingleThreadExecutor()
    capture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                try {
                    @Suppress("UnsafeOptInUsageError")
                    val mediaImage = image.image
                    if (mediaImage == null) {
                        onFailure(); image.close(); return
                    }
                    val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
                    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    recognizer.process(inputImage)
                        .addOnSuccessListener { visionText ->
                            val lines = visionText.textBlocks.flatMap { block -> block.lines.map { it.text } }
                            onResult(lines)
                        }
                        .addOnFailureListener { onFailure() }
                        .addOnCompleteListener { image.close() }
                } catch (e: Exception) {
                    onFailure()
                    image.close()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onFailure()
            }
        },
    )
}

private typealias ImageCaptureException = androidx.camera.core.ImageCaptureException

private suspend fun recognizeRawText(image: InputImage): List<String> {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val visionText = recognizer.process(image).await()
    return visionText.textBlocks.flatMap { block -> block.lines.map { it.text } }
}
