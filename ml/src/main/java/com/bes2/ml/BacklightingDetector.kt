package com.bes2.ml

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class BacklightingDetector @Inject constructor() {

    // Use FAST mode as we only need the bounding box, not landmarks/contours
    private val detectorOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .build()

    private val detector = FaceDetection.getClient(detectorOpts)

    /**
     * Analyzes the bitmap to check if the faces are backlit (darker than background).
     * Returns TRUE if backlighting is detected (i.e., bad quality).
     */
    suspend fun isBacklit(bitmap: Bitmap): Boolean {
        val image = InputImage.fromBitmap(bitmap, 0)

        return try {
            val faces = detector.process(image).await()
            
            // If no faces, we cannot judge backlighting based on face exposure.
            // We assume it's NOT backlit (pass) if no face is found.
            if (faces.isEmpty()) {
                return false
            }

            // 1. Calculate overall image luminance (brightness)
            // Optimization: Scale down for brightness calc to save CPU
            val scaledBitmap = if (bitmap.width > 200) {
                val ratio = 200.0f / bitmap.width
                Bitmap.createScaledBitmap(bitmap, 200, (bitmap.height * ratio).toInt(), false)
            } else {
                bitmap
            }
            
            val globalLuminance = calculateAverageLuminance(scaledBitmap)
            
            // If separate bitmap was created, recycle it
            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }

            // 2. Check each face
            var isAnyFaceTooDark = false
            
            for (face in faces) {
                val bounds = face.boundingBox
                
                // Ensure bounds are within bitmap dimensions
                val x = max(0, bounds.left)
                val y = max(0, bounds.top)
                val w = min(bitmap.width - x, bounds.width())
                val h = min(bitmap.height - y, bounds.height())

                if (w <= 0 || h <= 0) continue

                // Create a sub-bitmap for the face region
                val faceBitmap = Bitmap.createBitmap(bitmap, x, y, w, h)
                val faceLuminance = calculateAverageLuminance(faceBitmap)
                faceBitmap.recycle()

                Timber.d("[Backlight] Face Lum: $faceLuminance, Global Lum: $globalLuminance")

                // Thresholds:
                // 1. Absolute Darkness: Face is extremely dark (< 40)
                // 2. Relative Darkness: Background is significantly brighter than face (> 60 diff)
                
                val isAbsoluteDark = faceLuminance < 40
                val isRelativeDark = (globalLuminance - faceLuminance) > 60

                if (isAbsoluteDark || (isRelativeDark && faceLuminance < 100)) {
                    Timber.w("Backlighting detected! Face too dark ($faceLuminance) vs Global ($globalLuminance)")
                    isAnyFaceTooDark = true
                    break // One bad face is enough to reject (or count it?) - Strict rule: Reject
                }
            }

            isAnyFaceTooDark

        } catch (e: Exception) {
            Timber.e(e, "Error during backlighting detection")
            false // Fail safe: assume not backlit
        }
    }

    private fun calculateAverageLuminance(bitmap: Bitmap): Double {
        var sumLuminance = 0.0
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (pixel in pixels) {
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            
            // Standard luminance formula (Rec. 709)
            // Y = 0.2126R + 0.7152G + 0.0722B
            val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
            sumLuminance += luminance
        }

        return if (pixels.isNotEmpty()) sumLuminance / pixels.size else 0.0
    }
}
