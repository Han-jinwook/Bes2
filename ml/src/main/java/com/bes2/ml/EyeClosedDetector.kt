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
    
    // [MODIFIED] Increased threshold to 0.4 (Strict but safe for small faces)
    private val EYE_OPEN_THRESHOLD = 0.4f
    
    // [ADDED] Minimum Face Ratio (Face Area / Image Area) to consider eye status
    // If face is too small (< 3% of image), we can't reliably detect eyes -> assume open.
    private val MIN_FACE_RATIO = 0.03f

    suspend fun areEyesClosed(bitmap: Bitmap): Boolean {
        val image = InputImage.fromBitmap(bitmap, 0)
        val imageArea = bitmap.width * bitmap.height
        
        return try {
            val faces = detector.process(image).await()
            if (faces.isEmpty()) {
                return false
            }
            
            var eyeClosedDetected = false
            faces.forEachIndexed { index, face ->
                // Check face size
                val faceArea = face.boundingBox.width() * face.boundingBox.height()
                val ratio = faceArea.toFloat() / imageArea.toFloat()
                
                if (ratio < MIN_FACE_RATIO) {
                    Timber.d("[EyeCheck] Face #$index too small ($ratio). Skipping.")
                    return@forEachIndexed // Skip this face
                }
                
                val leftProb = face.leftEyeOpenProbability
                val rightProb = face.rightEyeOpenProbability
                
                val isLeftEyeClosed = leftProb?.let { it < EYE_OPEN_THRESHOLD } ?: false
                val isRightEyeClosed = rightProb?.let { it < EYE_OPEN_THRESHOLD } ?: false

                if (isLeftEyeClosed || isRightEyeClosed) {
                    Timber.i("[EyeCheck] DETECTED CLOSED! Face #$index. Left=$leftProb, Right=$rightProb")
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
