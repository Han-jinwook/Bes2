package com.bes2.ml

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class EyeClosedDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val highAccuracyOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(highAccuracyOpts)

    suspend fun areEyesClosed(bitmap: Bitmap): Boolean {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val faces = detector.process(image).await()
            if (faces.isEmpty()) {
                Timber.d("No faces detected in EyeClosedDetector, returning false.")
                return false
            }
            
            var eyeClosedDetected = false
            faces.forEachIndexed { index, face ->
                val leftProb = face.leftEyeOpenProbability
                val rightProb = face.rightEyeOpenProbability
                
                Timber.i("[EyeCheck] Face #$index: LeftOpen=$leftProb, RightOpen=$rightProb")

                // Threshold set to 0.5 as requested
                val isLeftEyeClosed = leftProb?.let { it < 0.5 } ?: false
                val isRightEyeClosed = rightProb?.let { it < 0.5 } ?: false

                if (isLeftEyeClosed || isRightEyeClosed) {
                    Timber.w("[EyeCheck] Eye closed DETECTED! Face #$index. LeftClosed=$isLeftEyeClosed ($leftProb), RightClosed=$isRightEyeClosed ($rightProb)")
                    eyeClosedDetected = true
                }
            }
            return eyeClosedDetected
        } catch (e: Exception) {
            Timber.e(e, "Error detecting faces for eye-closing analysis")
            false
        }
    }
}
