package com.example.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.AppLanguage
import com.example.data.model.StudentProfile
import com.example.ui.HomeworkState
import com.example.ui.theme.fontFamilyForLanguage

private const val CAM_TAG = "HomeworkCamera"

@Composable
fun HomeworkCameraScreen(
    homeworkState: HomeworkState,
    activeProfile: StudentProfile?,
    language: AppLanguage,
    onSetCameraActive: (Boolean) -> Unit,
    onSwitchCameraLens: () -> Unit,
    onPhotoCaptured: (Bitmap) -> Unit,
    onRetake: () -> Unit,
    onEditableQuestionChange: (String) -> Unit,
    onReAnalyzeCorrected: () -> Unit,
    onChildAttemptChange: (String) -> Unit,
    onSubmitAttempt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraError by remember { mutableStateOf<String?>(null) }
    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }
    val frontCameraAllowed = activeProfile?.cameraPermissionForChild == true

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraError = null
            onSetCameraActive(true)
        } else {
            cameraError = "Camera permission is needed only if you want live capture. You can still upload a homework photo or use the app with camera off!"
        }
    }

    // Zero-permission Android Photo Picker (Play Policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val src = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(src) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                onPhotoCaptured(bmp)
            } catch (e: Exception) {
                cameraError = "Could not open photo: ${e.localizedMessage}"
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Homework & Show-and-Tell Camera",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Camera stays OFF by default and only analyzes what you choose to show. No raw video or audio is stored.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Controls when camera is off
            if (!homeworkState.isCameraActive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("start_camera_button")
                    ) {
                        Icon(Icons.Rounded.CameraAlt, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Turn On Camera")
                    }
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("upload_homework_photo_button")
                    ) {
                        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Upload Photo")
                    }
                }
            }

            if (!cameraError.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = cameraError!!,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Live CameraX Preview with Switch, Capture, and clearly visible STOP button
            if (homeworkState.isCameraActive) {
                val useFront = homeworkState.useFrontCamera && frontCameraAllowed
                Card(
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(290.dp)
                        ) {
                            val previewView = remember {
                                PreviewView(context).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                }
                            }
                            DisposableEffect(useFront, lifecycleOwner) {
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                                val executor = ContextCompat.getMainExecutor(context)
                                cameraProviderFuture.addListener({
                                    try {
                                        val provider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.surfaceProvider = previewView.surfaceProvider
                                        }
                                        val capture = ImageCapture.Builder().build()
                                        imageCaptureRef = capture
                                        val selector = if (useFront) {
                                            CameraSelector.DEFAULT_FRONT_CAMERA
                                        } else {
                                            CameraSelector.DEFAULT_BACK_CAMERA
                                        }
                                        provider.unbindAll()
                                        provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                                    } catch (e: Exception) {
                                        Log.e(CAM_TAG, "Camera bind failed", e)
                                        cameraError = "Camera preview unavailable on this device (${e.localizedMessage ?: "emulator"}). Use Upload Photo instead."
                                    }
                                }, executor)

                                onDispose {
                                    try {
                                        cameraProviderFuture.get().unbindAll()
                                    } catch (_: Exception) {
                                    }
                                }
                            }

                            AndroidView(
                                factory = { previewView },
                                modifier = Modifier.fillMaxSize()
                            )

                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = if (useFront) "Front Camera (Show & Tell)" else "Rear Camera (Homework)",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (!frontCameraAllowed) {
                                        cameraError = "Parent permission for Front Camera is off in ${activeProfile?.preferredName ?: "child"}'s Profile."
                                    } else {
                                        onSwitchCameraLens()
                                    }
                                },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Rounded.Cameraswitch, contentDescription = "Switch camera")
                                Spacer(Modifier.width(4.dp))
                                Text("Switch")
                            }

                            Button(
                                onClick = {
                                    val cap = imageCaptureRef ?: return@Button
                                    cap.takePicture(
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageCapturedCallback() {
                                            override fun onCaptureSuccess(image: ImageProxy) {
                                                val bmp = image.toBitmap()
                                                image.close()
                                                onPhotoCaptured(bmp)
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                cameraError = "Capture failed: ${exception.localizedMessage}"
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("capture_homework_button")
                            ) {
                                Text("Capture")
                            }

                            Button(
                                onClick = { onSetCameraActive(false) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("stop_camera_button")
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Stop camera")
                                Spacer(Modifier.width(4.dp))
                                Text("Stop")
                            }
                        }
                    }
                }
            }

            // Captured / Uploaded Photo Preview + Retake + Recognized Question Correction
            homeworkState.selectedBitmap?.let { bmp ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Selected homework photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onRetake,
                                modifier = Modifier.testTag("retake_homework_button")
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Retake / Clear")
                            }
                        }
                    }
                }
            }

            if (homeworkState.isAnalyzing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("Meera Didi is reading your homework photo...")
                }
            }

            homeworkState.errorMessage?.let { err ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(err, modifier = Modifier.padding(12.dp))
                }
            }

            // Editable Recognized Question + First-Step Hint
            homeworkState.analysis?.let { analysis ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Subject: ${analysis.subjectDetected} • ${if (analysis.imageClarityOk) "Clear Photo ✓" else "Needs Clearer Photo"}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (!analysis.imageClarityOk) {
                            Text(analysis.clarityFeedback, style = MaterialTheme.typography.bodyMedium)
                        }
                        OutlinedTextField(
                            value = homeworkState.editableQuestion,
                            onValueChange = onEditableQuestionChange,
                            label = { Text("Recognized Question (Tap to Correct)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("homework_recognized_question_input")
                        )
                        OutlinedButton(
                            onClick = onReAnalyzeCorrected,
                            modifier = Modifier.testTag("homework_reanalyze_button")
                        ) {
                            Text("Update & Explain First Step")
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("First Step Hint:", style = MaterialTheme.typography.labelLarge)
                                Text(
                                    text = analysis.firstStepExplanation,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = fontFamilyForLanguage(language)
                                    )
                                )
                                Text(
                                    text = analysis.guidingQuestion,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = fontFamilyForLanguage(language)
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        OutlinedTextField(
                            value = homeworkState.childAttemptText,
                            onValueChange = onChildAttemptChange,
                            label = { Text("Try the first step here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("homework_child_attempt_input")
                        )
                        Button(
                            onClick = onSubmitAttempt,
                            enabled = homeworkState.childAttemptText.isNotBlank() && !homeworkState.isAnalyzing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("homework_submit_attempt_button")
                        ) {
                            Text("Check My First Step")
                        }

                        if (homeworkState.coachFeedback.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = homeworkState.coachFeedback,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = fontFamilyForLanguage(language)
                                    ),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
