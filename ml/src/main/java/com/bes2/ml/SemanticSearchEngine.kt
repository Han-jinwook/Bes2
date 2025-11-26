package com.bes2.ml

import android.content.Context
import android.graphics.Bitmap
import com.bes2.ml.util.CosineSimilarity
import com.bes2.ml.util.SimpleTokenizer
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
 * MobileCLIP based Semantic Search Engine.
 * Converts Images and Text into the same embedding space.
 */
@Singleton
class SemanticSearchEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenizer: SimpleTokenizer
) {

    companion object {
        private const val IMAGE_MODEL_FILE = "mobile_clip_image.tflite"
        private const val TEXT_MODEL_FILE = "mobile_clip_text.tflite"
        
        // MobileCLIP S0 usually outputs 512-dim vectors
        private const val EMBEDDING_SIZE = 512 
        private const val INPUT_IMAGE_SIZE = 224
    }

    private var imageInterpreter: Interpreter? = null
    private var textInterpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null

    init {
        initializeInterpreters()
    }

    private fun initializeInterpreters() {
        try {
            val options = Interpreter.Options()
            
            // 1. Try to use GPU Delegate for speed
            val compatList = CompatibilityList()
            if (compatList.isDelegateSupportedOnThisDevice) {
                val delegateOptions = compatList.bestOptionsForThisDevice
                gpuDelegate = GpuDelegate(delegateOptions)
                options.addDelegate(gpuDelegate)
                Timber.d("SemanticSearch: GPU Delegate initialized.")
            } else {
                Timber.w("SemanticSearch: GPU not supported, falling back to CPU.")
            }

            // 2. Load Models (Check if assets exist first to avoid crash during dev)
            if (isFileExistsInAssets(IMAGE_MODEL_FILE)) {
                val imageModel = FileUtil.loadMappedFile(context, IMAGE_MODEL_FILE)
                imageInterpreter = Interpreter(imageModel, options)
                Timber.d("SemanticSearch: Image Model loaded.")
            }

            if (isFileExistsInAssets(TEXT_MODEL_FILE)) {
                val textModel = FileUtil.loadMappedFile(context, TEXT_MODEL_FILE)
                textInterpreter = Interpreter(textModel, options)
                Timber.d("SemanticSearch: Text Model loaded.")
            }

        } catch (e: Exception) {
            Timber.e(e, "SemanticSearch: Failed to initialize AI models.")
        }
    }

    /**
     * Converts a Bitmap into a 512-dimensional feature vector.
     */
    fun encodeImage(bitmap: Bitmap): FloatArray? {
        val interpreter = imageInterpreter ?: return null

        return try {
            // Preprocess Image (Resize -> Normalize)
            // Note: Actual MobileCLIP might need specific normalization (mean/std).
            // Here we use standard 224x224 resize.
            val imageProcessor = org.tensorflow.lite.support.image.ImageProcessor.Builder()
                .add(ResizeOp(INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                // .add(NormalizeOp(...)) // Add specific normalization if required by the model
                .build()

            var tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
            tensorImage.load(bitmap)
            tensorImage = imageProcessor.process(tensorImage)

            // Output container
            val outputBuffer = ByteBuffer.allocateDirect(EMBEDDING_SIZE * 4) // Float (4 bytes)
            // Depends on model output shape: usually [1, 512]
            // val outputMap = mapOf(0 to outputBuffer) // Use this if multiple outputs

            // Run Inference
            interpreter.run(tensorImage.buffer, outputBuffer)

            // Convert ByteBuffer to FloatArray
            outputBuffer.rewind()
            val floatArray = FloatArray(EMBEDDING_SIZE)
            outputBuffer.asFloatBuffer().get(floatArray)
            
            floatArray
        } catch (e: Exception) {
            Timber.e(e, "SemanticSearch: Image encoding failed.")
            null
        }
    }

    /**
     * Converts text query into a feature vector.
     */
    fun encodeText(text: String): FloatArray? {
        val interpreter = textInterpreter ?: return null

        return try {
            // 1. Tokenize Text
            val tokenIds = tokenizer.tokenize(text)
            
            // 2. Prepare Output Buffer
            val outputBuffer = ByteBuffer.allocateDirect(EMBEDDING_SIZE * 4)

            // 3. Run Inference (Input: IntArray [1, 77], Output: FloatBuffer [1, 512])
            // Note: Interpreter expects inputs to be shaped correctly.
            // If the model expects [1, 77], passing IntArray might need wrapping in ByteBuffer or Object[].
            // For simple single-input models, run(Object input, Object output) works.
            // We pass IntArray (tokenIds) directly if model input type matches.
            // Some models expect int32 tensor, so IntArray works.
            
            // However, to be safe with shapes, let's reshape if needed.
            // For now, assuming model accepts [77] or [1, 77].
            
            // To be 100% safe with Int32 inputs:
            val inputBuffer = ByteBuffer.allocateDirect(tokenIds.size * 4)
            inputBuffer.asIntBuffer().put(tokenIds)
            
            interpreter.run(inputBuffer, outputBuffer)

            outputBuffer.rewind()
            val floatArray = FloatArray(EMBEDDING_SIZE)
            outputBuffer.asFloatBuffer().get(floatArray)
            
            floatArray
        } catch (e: Exception) {
            Timber.e(e, "SemanticSearch: Text encoding failed.")
            null
        }
    }
    
    fun calculateSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
        return CosineSimilarity.compute(vec1, vec2)
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
        imageInterpreter?.close()
        textInterpreter?.close()
        gpuDelegate?.close()
    }
}
