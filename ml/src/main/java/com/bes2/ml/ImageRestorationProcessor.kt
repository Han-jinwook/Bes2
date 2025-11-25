package com.bes2.ml

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import timber.log.Timber
import java.io.IOException
import java.nio.ByteBuffer
import javax.inject.Inject

/**
 * Processor for restoring/enhancing image quality using TFLite models (e.g., ESRGAN).
 * Supports dynamic input resizing based on model signature.
 */
class ImageRestorationProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var interpreter: Interpreter? = null
    private val MODEL_NAME = "esrgan.tflite"

    init {
        setupInterpreter()
    }

    private fun setupInterpreter() {
        try {
            val modelFile = FileUtil.loadMappedFile(context, MODEL_NAME)
            val options = Interpreter.Options()
            // Use NNAPI or GPU delegate if available for performance (Optional)
            // options.addDelegate(NnApiDelegate()) 
            interpreter = Interpreter(modelFile, options)
            Timber.d("ImageRestorationProcessor initialized with model: $MODEL_NAME")
        } catch (e: IOException) {
            Timber.w("Model file '$MODEL_NAME' not found in assets. Restoration will be disabled.")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize ImageRestorationProcessor")
        }
    }

    /**
     * Attempts to restore (super-resolve or deblur) the given bitmap.
     * If the model is not available or fails, returns the original bitmap.
     */
    suspend fun restore(originalBitmap: Bitmap): Bitmap {
        if (interpreter == null) {
            Timber.w("Interpreter is null, returning original bitmap.")
            return originalBitmap
        }

        return try {
            Timber.d("Starting image restoration...")
            
            // 1. Inspect Model Input/Output
            val inputTensor = interpreter!!.getInputTensor(0)
            val inputShape = inputTensor.shape() // [1, H, W, 3] usually
            // Handle dynamic shapes if -1 is present, otherwise use fixed
            val targetHeight = if (inputShape[1] > 0) inputShape[1] else 50 // Default if dynamic
            val targetWidth = if (inputShape[2] > 0) inputShape[2] else 50   // Default if dynamic
            val inputDataType = inputTensor.dataType()

            Timber.d("Model Input: Shape=${inputShape.contentToString()}, Type=$inputDataType")

            // 2. Preprocess Input
            val imageProcessorBuilder = ImageProcessor.Builder()
                .add(ResizeOp(targetHeight, targetWidth, ResizeOp.ResizeMethod.BILINEAR))

            // Normalization: ESRGAN usually expects [0, 255] -> [0, 1] (Float) or [0, 255] (Uint8)
            if (inputDataType == DataType.FLOAT32) {
                // If float, usually needs normalization to [0, 1] or [-1, 1]
                // Most ESRGAN TFLite models expect [0, 255] float inputs (no div) OR [0, 1]
                // Let's try standard [0, 255] float first (CastOp)
                imageProcessorBuilder.add(CastOp(DataType.FLOAT32))
                // If model output is black, try adding NormalizeOp(0f, 255f) here.
                // Standard TF Hub ESRGAN takes [0, 255] floats.
            } else if (inputDataType == DataType.UINT8) {
                imageProcessorBuilder.add(CastOp(DataType.UINT8))
            }

            val imageProcessor = imageProcessorBuilder.build()
            var tImage = TensorImage(inputDataType)
            tImage.load(originalBitmap)
            tImage = imageProcessor.process(tImage)

            // 3. Prepare Output Buffer
            val outputTensor = interpreter!!.getOutputTensor(0)
            val outputShape = outputTensor.shape()
            val outputDataType = outputTensor.dataType()
            
            // If dynamic output shape (e.g. [1, -1, -1, 3]), we might need to resize output buffer?
            // TFLite interpreter.run handles buffer resizing if using TensorBuffer? No, usually fixed.
            // For ESRGAN x4, output dim is input * 4.
            val outHeight = if (outputShape[1] > 0) outputShape[1] else targetHeight * 4
            val outWidth = if (outputShape[2] > 0) outputShape[2] else targetWidth * 4
            
            // Create output TensorBuffer
            val outputBuffer = TensorBuffer.createFixedSize(
                intArrayOf(1, outHeight, outWidth, 3), 
                outputDataType
            )

            // 4. Run Inference
            Timber.d("Running inference...")
            interpreter!!.run(tImage.buffer, outputBuffer.buffer.rewind())

            // 5. Postprocess Output
            Timber.d("Post-processing output...")
            
            // Convert output buffer to Bitmap
            // Output is likely Float32 [0, 255] or [0, 1]. Need to clamp and convert to ARGB.
            val outputBitmap = convertOutputToBitmap(outputBuffer, outWidth, outHeight, outputDataType)
            
            Timber.d("Restoration complete.")
            return outputBitmap

        } catch (e: Exception) {
            Timber.e(e, "Error during image restoration")
            return originalBitmap
        }
    }
    
    private fun convertOutputToBitmap(
        tensorBuffer: TensorBuffer, 
        width: Int, 
        height: Int, 
        dataType: DataType
    ): Bitmap {
        val floatArray = tensorBuffer.floatArray
        val intValues = IntArray(width * height)
        
        // Denormalization if needed. If model outputs [0,1], multiply by 255.
        // Assuming [0, 255] range for standard ESRGAN.
        val scale = if (dataType == DataType.FLOAT32) 1.0f else 1.0f 
        // If output is very dark, change scale to 255.0f
        
        for (i in 0 until width * height) {
            val r = (floatArray[i * 3] * scale).coerceIn(0f, 255f).toInt()
            val g = (floatArray[i * 3 + 1] * scale).coerceIn(0f, 255f).toInt()
            val b = (floatArray[i * 3 + 2] * scale).coerceIn(0f, 255f).toInt()
            
            // ARGB: Alpha is always 255 (Opaque)
            intValues[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
        
        return Bitmap.createBitmap(intValues, width, height, Bitmap.Config.ARGB_8888)
    }
    
    fun close() {
        interpreter?.close()
    }
}
