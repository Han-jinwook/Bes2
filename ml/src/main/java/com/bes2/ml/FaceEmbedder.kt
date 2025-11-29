package com.bes2.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import timber.log.Timber
import java.nio.ByteBuffer
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class FaceEmbedder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var interpreter: Interpreter
    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    )

    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(IMAGE_SIZE, IMAGE_SIZE, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(NORM_MEAN, NORM_STD))
        .build()

    init {
        val model = FileUtil.loadMappedFile(context, MODEL_FILE)
        interpreter = Interpreter(model, Interpreter.Options())
        Timber.d("FaceNet model loaded and interpreter created.")
    }

    suspend fun getFaceEmbedding(bitmap: Bitmap): FloatArray? {
        // 1. ML Kit으로 얼굴 찾기
        val faces = try {
            faceDetector.process(InputImage.fromBitmap(bitmap, 0)).await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to detect faces for embedding.")
            return null
        }

        if (faces.isEmpty()) {
            Timber.d("No face found for embedding generation.")
            return null
        }
        
        // 첫 번째 얼굴만 사용
        val face = faces.first()

        // 2. 얼굴 영역을 잘라내고 정사각형으로 만들기
        val faceBitmap = cropFace(bitmap, face.boundingBox)

        // 3. 모델 입력에 맞게 이미지 전처리
        var tensorImage = TensorImage(TFLITE_DATA_TYPE)
        tensorImage.load(faceBitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // 4. 모델 실행하여 임베딩 추출
        val embeddingOutput = Array(1) { FloatArray(EMBEDDING_SIZE) }
        try {
            interpreter.run(tensorImage.buffer, embeddingOutput)
            Timber.d("Face embedding generated successfully.")
            return embeddingOutput[0]
        } catch (e: Exception) {
            Timber.e(e, "Failed to run FaceNet interpreter.")
            return null
        }
    }
    
    private fun cropFace(bitmap: Bitmap, boundingBox: Rect): Bitmap {
        // 얼굴 바운딩 박스를 정사각형으로 만듦 (모델 요구사항)
        val centerX = boundingBox.centerX()
        val centerY = boundingBox.centerY()
        val size = max(boundingBox.width(), boundingBox.height())
        val halfSize = size / 2

        val newLeft = max(0, centerX - halfSize)
        val newTop = max(0, centerY - halfSize)
        val newRight = min(bitmap.width, centerX + halfSize)
        val newBottom = min(bitmap.height, centerY + halfSize)
        
        val squareRect = Rect(newLeft, newTop, newRight, newBottom)

        return Bitmap.createBitmap(bitmap, squareRect.left, squareRect.top, squareRect.width(), squareRect.height())
    }

    companion object {
        private const val MODEL_FILE = "mobile_face_net.tflite"
        private const val IMAGE_SIZE = 112
        private const val EMBEDDING_SIZE = 192 // MobileFaceNet 모델의 출력 크기
        private const val NORM_MEAN = 127.5f
        private const val NORM_STD = 128.0f
        
        private val TFLITE_DATA_TYPE = org.tensorflow.lite.DataType.FLOAT32

        // [NEW] Cosine Similarity calculation function
        fun calculateCosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
            if (v1.size != v2.size) return 0f
            
            var dotProduct = 0.0f
            var norm1 = 0.0f
            var norm2 = 0.0f
            
            for (i in v1.indices) {
                dotProduct += v1[i] * v2[i]
                norm1 += v1[i] * v1[i]
                norm2 += v2[i] * v2[i]
            }
            
            return if (norm1 == 0.0f || norm2 == 0.0f) {
                0.0f
            } else {
                (dotProduct / (sqrt(norm1) * sqrt(norm2)))
            }
        }
    }
}
