package com.bes2.ml

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class SmileDetector @Inject constructor() {

    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(realTimeOpts)

    suspend fun getSmilingProbability(bitmap: Bitmap): Float? {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val faces = detector.process(image).await()
            if (faces.isEmpty()) {
                Timber.d("No faces detected in the image.")
                return null // 또는 기본값 0.5f를 반환하여 중립으로 처리
            }
            // 여러 얼굴이 감지된 경우, 가장 크게 나온 얼굴의 웃음 확률을 반환합니다.
            val largestFace = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
            Timber.d("Largest face smiling probability: ${largestFace?.smilingProbability}")
            return largestFace?.smilingProbability
        } catch (e: Exception) {
            Timber.e(e, "Error detecting smile")
            null
        }
    }
}
