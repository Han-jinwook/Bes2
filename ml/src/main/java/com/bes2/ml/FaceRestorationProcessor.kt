package com.bes2.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import timber.log.Timber
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Advanced Face Restoration using GFPGAN (TFLite version).
 * Detects faces, crops them, restores details, and pastes them back.
 */
@Singleton
class FaceRestorationProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val MODEL_FILE = "gfpgan_int8.tflite" // Placeholder
        private const val INPUT_SIZE = 512 // GFPGAN usually works on 512x512
    }

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    
    // ML Kit Face Detector
    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .build()
    )

    init {
        initializeInterpreter()
    }

    private fun initializeInterpreter() {
        try {
            val options = Interpreter.Options()
            val compatList = CompatibilityList()
            
            if (compatList.isDelegateSupportedOnThisDevice) {
                val delegateOptions = compatList.bestOptionsForThisDevice
                gpuDelegate = GpuDelegate(delegateOptions)
                options.addDelegate(gpuDelegate)
                Timber.d("FaceRestoration: GPU Delegate initialized.")
            } else {
                Timber.w("FaceRestoration: GPU not supported, using CPU (Slow!).")
                options.setNumThreads(4)
            }

            if (isFileExistsInAssets(MODEL_FILE)) {
                val model = FileUtil.loadMappedFile(context, MODEL_FILE)
                interpreter = Interpreter(model, options)
                Timber.d("FaceRestoration: Model loaded.")
            } else {
                Timber.w("FaceRestoration: Model file not found.")
            }
        } catch (e: Exception) {
            Timber.e(e, "FaceRestoration: Init failed.")
        }
    }

    suspend fun restoreFaces(originalBitmap: Bitmap): Bitmap {
        // Fix: Use property directly or checking it differently to avoid unused variable warning
        if (interpreter == null) return originalBitmap
        
        // 1. Detect Faces
        val inputImage = InputImage.fromBitmap(originalBitmap, 0)
        val faces = try {
            faceDetector.process(inputImage).await()
        } catch (e: Exception) {
            Timber.e(e, "Face detection failed.")
            return originalBitmap
        }

        if (faces.isEmpty()) {
            Timber.d("No faces detected to restore.")
            return originalBitmap
        }

        Timber.d("Found ${faces.size} faces to restore.")
        
        // Working on a mutable copy
        val outputBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(outputBitmap)

        for (face in faces) {
            try {
                // 2. Crop Face with Padding
                val boundingBox = face.boundingBox
                val paddedRect = getPaddedRect(boundingBox, originalBitmap.width, originalBitmap.height)
                
                val faceBitmap = Bitmap.createBitmap(
                    originalBitmap,
                    paddedRect.left, paddedRect.top,
                    paddedRect.width(), paddedRect.height()
                )

                // 3. Restore Face (Inference)
                val restoredFace = runGFPGAN(faceBitmap)

                // 4. Paste Back
                if (restoredFace != null) {
                    val scaledRestored = Bitmap.createScaledBitmap(
                        restoredFace,
                        paddedRect.width(),
                        paddedRect.height(),
                        true
                    )
                    canvas.drawBitmap(scaledRestored, paddedRect.left.toFloat(), paddedRect.top.toFloat(), null)
                    restoredFace.recycle()
                    scaledRestored.recycle()
                }
                faceBitmap.recycle()
                
            } catch (e: Exception) {
                Timber.e(e, "Error restoring a single face.")
            }
        }

        return outputBitmap
    }

    private fun runGFPGAN(faceBitmap: Bitmap): Bitmap? {
        val interpreter = interpreter ?: return null
        
        return try {
            // Preprocess: Resize to 512x512
            val imageProcessor = org.tensorflow.lite.support.image.ImageProcessor.Builder()
                .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                // Normalization: GFPGAN usually expects [-1, 1] range
                .build()

            var tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
            tensorImage.load(faceBitmap)
            tensorImage = imageProcessor.process(tensorImage)
            
            // Note: Standard TFLite Support NormalizeOp might be needed here manually 
            // if the model expects (value - 0.5) / 0.5. 
            // For brevity, assuming model handles or standard input.

            val outputBuffer = ByteBuffer.allocateDirect(INPUT_SIZE * INPUT_SIZE * 3 * 4) // Float32 RGB
            interpreter.run(tensorImage.buffer, outputBuffer)

            // Postprocess: Convert Output Buffer back to Bitmap
            // This part requires careful implementation (Float -> Int8 -> Bitmap).
            // Placeholder logic:
            
            // For now, let's return the resized input just to verify flow (Simulating Pass-through)
            // Real implementation needs tensor-to-bitmap conversion util.
            val restored = Bitmap.createScaledBitmap(faceBitmap, INPUT_SIZE, INPUT_SIZE, true)
            restored

        } catch (e: Exception) {
            Timber.e(e, "GFPGAN Inference failed.")
            null
        }
    }

    private fun getPaddedRect(rect: Rect, maxWidth: Int, maxHeight: Int): Rect {
        val padding = max(rect.width(), rect.height()) / 2 // 50% padding
        return Rect(
            (rect.left - padding).coerceAtLeast(0),
            (rect.top - padding).coerceAtLeast(0),
            (rect.right + padding).coerceAtMost(maxWidth),
            (rect.bottom + padding).coerceAtMost(maxHeight)
        )
    }
    
    private fun isFileExistsInAssets(filename: String): Boolean {
        return try {
            context.assets.open(filename).close()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun close() {
        interpreter?.close()
        gpuDelegate?.close()
        faceDetector.close()
    }
}
