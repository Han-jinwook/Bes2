package com.bes2.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import timber.log.Timber // Timber 임포트 추가
import java.nio.ByteBuffer

// Hilt를 사용하여 의존성 주입을 고려할 수 있지만, 우선은 직접 생성하는 방식으로 작성합니다.
// 필요하다면 @Inject constructor(...) 등을 추가할 수 있습니다.
class NimaQualityAnalyzer(context: Context, modelFileName: String = "model_nima_aesthetics.tflite") {

    private var interpreter: Interpreter? = null

    // NIMA 모델의 일반적인 입력 크기입니다. 실제 모델에 따라 다를 수 있으므로 확인이 필요합니다.
    private val inputImageWidth = 224
    private val inputImageHeight = 224
    private val numClasses = 10 // NIMA 모델은 보통 1점에서 10점까지의 점수 분포를 출력합니다.

    init {
        try {
            val modelByteBuffer: ByteBuffer = FileUtil.loadMappedFile(context, modelFileName)
            val options = Interpreter.Options()
            // 추후 GPU Delegate, NNAPI Delegate 사용 등을 고려할 수 있습니다.
            // options.addDelegate(...)
            interpreter = Interpreter(modelByteBuffer, options)
            Timber.d("NIMA interpreter initialized successfully for model: $modelFileName")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize NIMA interpreter for model: $modelFileName")
            interpreter = null // 초기화 실패 시 null로 설정
        }
    }

    /**
     * 주어진 Bitmap 이미지의 미적 품질 점수 분포를 반환합니다.
     * @param bitmap 평가할 이미지
     * @return 1점에서 10점까지의 점수 분포 (FloatArray), 또는 분석 실패 시 null
     */
    fun analyze(bitmap: Bitmap): FloatArray? {
        if (interpreter == null) {
            Timber.e("NIMA interpreter is not initialized. Cannot analyze image.")
            return null
        }

        try {
            val tensorImage = TensorImage.fromBitmap(bitmap) // Defaults to DataType.UINT8

            // 이미지 전처리: NIMA 모델의 입력 요구사항에 맞게 크기 조정 및 정규화
            // NormalizeOp(0f, 255f)를 사용하여 UINT8 [0,255] 입력을 FLOAT32 [0,1] 범위로 변환합니다.
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(inputImageHeight, inputImageWidth, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(0f, 255f)) // 픽셀 값을 0.0 ~ 1.0 범위로 정규화하고 FLOAT32로 변환
                .build()

            val processedImage = imageProcessor.process(tensorImage)
            // 데이터 타입이 FLOAT32로 변환되었는지 확인 (디버깅용)
            Timber.d("Image preprocessed successfully for NIMA. DataType: ${processedImage.dataType}, Buffer size: ${processedImage.buffer.capacity()}")


            // 모델 추론
            // 출력 버퍼의 크기는 [1, numClasses] 형태가 됩니다.
            val outputScores = Array(1) { FloatArray(numClasses) }
            interpreter?.run(processedImage.buffer, outputScores)
            Timber.d("NIMA model inference completed. Output scores: ${outputScores[0].contentToString()}")


            return outputScores[0] // 첫 번째 (그리고 유일한) 이미지의 점수 분포 반환

        } catch (e: Exception) {
            Timber.e(e, "Failed to analyze image with NIMA model. Bitmap size: ${bitmap.width}x${bitmap.height}")
            return null
        }
    }

    /**
     * NIMA 모델로부터 얻은 점수 분포를 사용하여 평균 점수를 계산합니다.
     * @param scores NIMA 모델의 출력 점수 분포 (10개의 Float 값)
     * @return 평균 미적 품질 점수 (1.0 ~ 10.0), 또는 입력이 유효하지 않으면 0.0f
     */
    fun calculateMeanScore(scores: FloatArray?): Float {
        if (scores == null || scores.size != numClasses) {
            Timber.w("Invalid scores array for mean calculation. Scores: ${scores?.contentToString()}")
            return 0.0f
        }
        var meanScore = 0.0f
        for (i in scores.indices) {
            meanScore += (i + 1) * scores[i]
        }
        Timber.d("Calculated mean NIMA score: $meanScore from scores: ${scores.contentToString()}")
        return meanScore
    }

    // 인터프리터 리소스 해제를 위한 메소드 (예: ViewModel의 onCleared 등에서 호출)
    fun close() {
        interpreter?.close()
        interpreter = null
        Timber.d("NIMA interpreter closed.")
    }
}
