package com.elachi.app.ui.recipe

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executors

// This screen allows users to capture recipe images using the camera or select existing screenshots from their device.
// It uses ML Kit Text Recognition (OCR) to extract recipe text from the selected or captured images.
// The recognised text is processed and structured into recipe details such as the title, ingredients, steps, servings, and cooking time.

private const val MAX_SCREENSHOTS = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraCaptureScreen(
    viewModel: CameraCaptureViewModel,
    onTextRecognized: (
        title: String,
        ingredients: List<DraftIngredient>,
        steps: List<String>,
        method: String,
        servings: String,
        cookTimeMinutes: String,
    ) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    var isProcessing by remember {
        mutableStateOf(false)
    }

    var processingMessage by remember {
        mutableStateOf("")
    }

    var errorText by remember {
        mutableStateOf<String?>(null)
    }

    var imageCapture by remember {
        mutableStateOf<ImageCapture?>(null)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun deliver(result: OcrResult) {
        onTextRecognized(
            result.title,
            result.ingredients,
            result.steps,
            result.method,
            result.servings,
            result.cookTimeMinutes,
        )
    }

    val screenshotPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(
            maxItems = MAX_SCREENSHOTS,
        ),
    ) { selectedUris ->
        if (selectedUris.isEmpty()) {
            return@rememberLauncherForActivityResult
        }

        isProcessing = true
        errorText = null
        processingMessage =
            "Preparing ${selectedUris.size} screenshot(s)..."

        scope.launch {
            val combinedRawLines = mutableListOf<String>()
            var successfulScreenshots = 0

            selectedUris.forEachIndexed { index, uri ->
                processingMessage =
                    "Reading screenshot ${index + 1} of ${selectedUris.size}..."

                try {
                    val inputImage = InputImage.fromFilePath(
                        context,
                        uri,
                    )

                    val screenshotLines = recognizeRawText(inputImage)
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

                    if (screenshotLines.isNotEmpty()) {

                        if (combinedRawLines.isNotEmpty()) {
                            combinedRawLines.add("")
                        }

                        combinedRawLines.addAll(screenshotLines)
                        successfulScreenshots++
                    }
                } catch (exception: Exception) {
                }
            }

            if (combinedRawLines.isEmpty()) {
                isProcessing = false
                processingMessage = ""
                errorText =
                    "No readable recipe text was found. " +
                            "Choose clearer screenshots or enter the recipe manually."

                return@launch
            }

            processingMessage =
                "Structuring text from $successfulScreenshots screenshot(s)..."

            viewModel.processRecognizedText(
                rawLines = combinedRawLines,
            ) { result ->
                isProcessing = false
                processingMessage = ""
                deliver(result)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Capture Recipe")
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            if (hasCameraPermission) {
                AndroidView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    factory = { cameraContext ->
                        val previewView = PreviewView(cameraContext)

                        val cameraProviderFuture =
                            ProcessCameraProvider.getInstance(cameraContext)

                        cameraProviderFuture.addListener(
                            {
                                try {
                                    val cameraProvider =
                                        cameraProviderFuture.get()

                                    val preview =
                                        Preview.Builder()
                                            .build()
                                            .also {
                                                it.setSurfaceProvider(
                                                    previewView.surfaceProvider,
                                                )
                                            }

                                    val capture =
                                        ImageCapture.Builder()
                                            .build()

                                    imageCapture = capture

                                    cameraProvider.unbindAll()

                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        capture,
                                    )
                                } catch (exception: Exception) {
                                    errorText =
                                        "The camera could not be started."
                                }
                            },
                            ContextCompat.getMainExecutor(cameraContext),
                        )

                        previewView
                    },
                )
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Camera permission was not granted. " +
                                "You can still select screenshots below.",
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }

            if (isProcessing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = processingMessage,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            errorText?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Select up to $MAX_SCREENSHOTS screenshots. " +
                            "Choose them in the same order as the recipe pages.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedButton(
                    onClick = {
                        screenshotPicker.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts
                                    .PickVisualMedia
                                    .ImageOnly,
                            ),
                        )
                    },
                    enabled = !isProcessing,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Collections,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )

                    Spacer(modifier = Modifier.size(6.dp))

                    Text("Choose Screenshots")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        enabled = !isProcessing,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val capture = imageCapture
                                ?: return@Button

                            isProcessing = true
                            errorText = null
                            processingMessage =
                                "Reading captured image..."

                            captureRawText(
                                capture = capture,
                                onResult = { rawLines ->
                                    if (rawLines.none { it.isNotBlank() }) {
                                        isProcessing = false
                                        processingMessage = ""
                                        errorText =
                                            "No readable text was found."
                                    } else {
                                        processingMessage =
                                            "Structuring recipe..."

                                        viewModel.processRecognizedText(
                                            rawLines = rawLines,
                                        ) { result ->
                                            isProcessing = false
                                            processingMessage = ""
                                            deliver(result)
                                        }
                                    }
                                },
                                onFailure = {
                                    isProcessing = false
                                    processingMessage = ""
                                    errorText =
                                        "Text could not be recognised. " +
                                                "Try again with better lighting."
                                },
                            )
                        },
                        enabled =
                            hasCameraPermission &&
                                    !isProcessing &&
                                    imageCapture != null,
                        modifier = Modifier.weight(1f),
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color =
                                    MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )

                            Spacer(modifier = Modifier.size(6.dp))

                            Text("Capture")
                        }
                    }
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
    val executor = Executors.newSingleThreadExecutor()

    capture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(
                image: androidx.camera.core.ImageProxy,
            ) {
                try {
                    @Suppress("UnsafeOptInUsageError")
                    val mediaImage = image.image

                    if (mediaImage == null) {
                        image.close()
                        executor.shutdown()
                        onFailure()
                        return
                    }

                    val inputImage = InputImage.fromMediaImage(
                        mediaImage,
                        image.imageInfo.rotationDegrees,
                    )

                    val recognizer = TextRecognition.getClient(
                        TextRecognizerOptions.DEFAULT_OPTIONS,
                    )

                    recognizer.process(inputImage)
                        .addOnSuccessListener { visionText ->
                            val lines = visionText.textBlocks.flatMap {
                                    block ->
                                block.lines.map { line ->
                                    line.text
                                }
                            }

                            onResult(lines)
                        }
                        .addOnFailureListener {
                            onFailure()
                        }
                        .addOnCompleteListener {
                            recognizer.close()
                            image.close()
                            executor.shutdown()
                        }
                } catch (exception: Exception) {
                    image.close()
                    executor.shutdown()
                    onFailure()
                }
            }

            override fun onError(
                exception: androidx.camera.core.ImageCaptureException,
            ) {
                executor.shutdown()
                onFailure()
            }
        },
    )
}

private suspend fun recognizeRawText(
    image: InputImage,
): List<String> {
    val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS,
    )

    return try {
        val visionText = recognizer.process(image).await()

        visionText.textBlocks.flatMap { block ->
            block.lines.map { line ->
                line.text
            }
        }
    } finally {
        recognizer.close()
    }
}