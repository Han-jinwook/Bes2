package com.bes2.ml

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
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

/**
 * MUSIQ (Multi-scale Image Quality Transformer) Analyzer.
 * Evaluates aesthetic quality and composition.
 * Slower but more accurate than NIMA.
 */
@Singleton
class MusiqQualityAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val MODEL_FILE = "musiq_spaq_koniq.tflite" // Placeholder name
        private const val INPUT_SIZE = 384 // MUSIQ usually requires higher res
    }

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null

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
                Timber.d("MUSIQ: GPU Delegate initialized.")
            } else {
                Timber.w("MUSIQ: GPU not supported, using CPU.")
            }

            if (isFileExistsInAssets(MODEL_FILE)) {
                val model = FileUtil.loadMappedFile(context, MODEL_FILE)
                interpreter = Interpreter(model, options)
                Timber.d("MUSIQ: Model loaded successfully.")
            } else {
                Timber.w("MUSIQ: Model file not found in assets.")
            }

        } catch (e: Exception) {
            Timber.e(e, "MUSIQ: Failed to initialize model.")
        }
    }

    /**
     * Returns a aesthetic score (0.0 ~ 10.0 or 0.0 ~ 1.0 depending on model).
     * Usually MUSIQ outputs a single float score directly.
     */
    fun analyze(bitmap: Bitmap): Float {
        val interpreter = interpreter ?: return 0f // Fallback if model missing

        return try {
            // Preprocess
            val imageProcessor = org.tensorflow.lite.support.image.ImageProcessor.Builder()
                .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                // Normalization is model-specific. Usually 0-1 or -1 to 1.
                // Assuming standard TFLite Support handles basic scaling.
                .build()

            var tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
            tensorImage.load(bitmap)
            tensorImage = imageProcessor.process(tensorImage)

            // Output: Single Float Score
            val outputBuffer = ByteBuffer.allocateDirect(4) 
            interpreter.run(tensorImage.buffer, outputBuffer)

            outputBuffer.rewind()
            val score = outputBuffer.asFloatBuffer().get(0)
            
            Timber.d("MUSIQ Score: $score")
            score

        } catch (e: Exception) {
            Timber.e(e, "MUSIQ: Analysis failed.")
            0f
        }
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
    }
}
