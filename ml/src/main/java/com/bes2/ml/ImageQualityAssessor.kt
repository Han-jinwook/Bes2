package com.bes2.ml

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import timber.log.Timber
import kotlin.math.pow
import kotlin.math.max
import kotlin.math.roundToInt

object ImageQualityAssessor {

    private const val MAX_PROCESSING_DIMENSION = 1024f // 최대 처리 이미지 크기 (가장 긴 변 기준)

    /**
     * 이미지의 흐림 정도를 라플라시안 분산을 사용하여 계산합니다.
     * 점수가 낮을수록 이미지가 흐릿하다고 판단할 수 있습니다.
     *
     * @param bitmap 분석할 Bitmap 이미지.
     * @return 라플라시안 분산 값 (블러 점수).
     */
    fun calculateBlurScore(bitmap: Bitmap): Float {
        Timber.d("Calculating blur score for original bitmap: ${bitmap.width}x${bitmap.height}")
        if (bitmap.width == 0 || bitmap.height == 0) {
            Timber.w("Bitmap has zero width or height for blur score.")
            return 0.0f
        }

        var resizedBitmap: Bitmap? = null
        val bitmapToProcess: Bitmap

        if (bitmap.width > MAX_PROCESSING_DIMENSION || bitmap.height > MAX_PROCESSING_DIMENSION) {
            val scaleFactor = MAX_PROCESSING_DIMENSION / max(bitmap.width, bitmap.height)
            val newWidth = (bitmap.width * scaleFactor).roundToInt()
            val newHeight = (bitmap.height * scaleFactor).roundToInt()
            resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            bitmapToProcess = resizedBitmap
            Timber.d("Resized bitmap for blur score to: ${newWidth}x${newHeight}")
        } else {
            bitmapToProcess = bitmap
        }

        try {
            val grayscaleBitmap = convertToGrayscale(bitmapToProcess)
            Timber.d("Converted to grayscale for blur detection.")

            val width = grayscaleBitmap.width
            val height = grayscaleBitmap.height
            // IntArray는 ARGB_8888 형식의 픽셀 데이터를 저장하므로 각 int는 4바이트입니다.
            // grayscaleBitmap은 ARGB_8888로 생성되지만, 실제로는 R,G,B 값이 동일한 회색조입니다.
            val pixels = IntArray(width * height)
            grayscaleBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            // convertToGrayscale에서 생성된 grayscaleBitmap은 여기서 사용 후 GC 대상이 됩니다.
            // 만약 grayscaleBitmap도 명시적으로 recycle 하려면, 이 함수 내에서 더 이상 사용되지 않는 시점에 해야합니다.
            // 하지만 여기서는 getPixels 이후 바로 사용되므로, 지역 변수로서 역할이 끝나면 GC가 처리하도록 둡니다.

            var laplacianMean = 0.0
            var laplacianVarianceSum = 0.0
            // DoubleArray는 각 double이 8바이트입니다.
            // 리사이징으로 width*height가 줄어들어 이 배열의 크기도 크게 감소합니다.
            val laplacianValues = DoubleArray(width * height) 
            var count = 0

            // 경계 픽셀을 제외하고 라플라시안 계산
            for (y in 1 until height - 1) {
                for (x in 1 until width - 1) {
                    val pCenter = pixels[y * width + x] and 0xFF // Blue 채널 (회색조에서는 R,G,B 동일)
                    val pUp = pixels[(y - 1) * width + x] and 0xFF
                    val pDown = pixels[(y + 1) * width + x] and 0xFF
                    val pLeft = pixels[y * width + (x - 1)] and 0xFF
                    val pRight = pixels[y * width + (x + 1)] and 0xFF

                    val laplacian = (pUp + pDown + pLeft + pRight - 4 * pCenter).toDouble()
                    if (count < laplacianValues.size) { 
                        laplacianValues[count] = laplacian
                        laplacianMean += laplacian
                        count++
                    }
                }
            }

            if (count == 0) {
                Timber.w("Not enough pixels to calculate Laplacian variance (after excluding borders).")
                return 0.0f
            }
            laplacianMean /= count

            for (i in 0 until count) {
                laplacianVarianceSum += (laplacianValues[i] - laplacianMean).pow(2)
            }

            val variance = (laplacianVarianceSum / count).toFloat()
            Timber.i("Calculated blur score (Laplacian Variance): $variance for processed size ${width}x${height}")
            return variance

        } catch (e: OutOfMemoryError) {
            // OOM 발생 시에도 Timber 로깅을 시도합니다. (로깅 자체가 실패할 수도 있지만 최선)
            Timber.e(e, "OutOfMemoryError calculating blur score even after potential resize. Processed bitmap: ${bitmapToProcess.width}x${bitmapToProcess.height}")
            return 0.0f // OOM 발생 시 0점 반환
        } catch (e: Exception) {
            Timber.e(e, "Error calculating blur score for processed bitmap: ${bitmapToProcess.width}x${bitmapToProcess.height}")
            return 0.0f
        } finally {
            resizedBitmap?.recycle() // 리사이징된 비트맵이 생성되었다면 명시적으로 재활용
            Timber.d("Recycled resizedBitmap for blur score if it was created.")
        }
    }

    /**
     * 이미지의 평균 밝기를 계산하여 노출 점수를 반환합니다.
     * 점수는 0(매우 어두움)에서 255(매우 밝음) 사이의 값을 가집니다.
     *
     * @param bitmap 분석할 Bitmap 이미지.
     * @return 평균 픽셀 강도 (노출 점수).
     */
    fun calculateExposureScore(bitmap: Bitmap): Float {
        Timber.d("Calculating exposure score for original bitmap: ${bitmap.width}x${bitmap.height}")
        if (bitmap.width == 0 || bitmap.height == 0) {
            Timber.w("Bitmap has zero width or height for exposure score.")
            return 0.0f
        }

        var resizedBitmap: Bitmap? = null
        val bitmapToProcess: Bitmap

        if (bitmap.width > MAX_PROCESSING_DIMENSION || bitmap.height > MAX_PROCESSING_DIMENSION) {
            val scaleFactor = MAX_PROCESSING_DIMENSION / max(bitmap.width, bitmap.height)
            val newWidth = (bitmap.width * scaleFactor).roundToInt()
            val newHeight = (bitmap.height * scaleFactor).roundToInt()
            resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            bitmapToProcess = resizedBitmap
            Timber.d("Resized bitmap for exposure score to: ${newWidth}x${newHeight}")
        } else {
            bitmapToProcess = bitmap
        }

        try {
            val grayscaleBitmap = convertToGrayscale(bitmapToProcess)
            Timber.d("Converted to grayscale for exposure detection.")

            val width = grayscaleBitmap.width
            val height = grayscaleBitmap.height
            val pixels = IntArray(width * height)
            grayscaleBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            // convertToGrayscale에서 생성된 grayscaleBitmap은 여기서 사용 후 GC 대상이 됩니다.

            var sumOfBrightness: Long = 0
            for (i in 0 until width * height) {
                sumOfBrightness += pixels[i] and 0xFF // Blue 채널 (회색조에서는 R,G,B 동일)
            }

            val averageBrightness = if (width * height == 0) 0f else sumOfBrightness.toFloat() / (width * height)
            Timber.i("Calculated exposure score (Average Brightness): $averageBrightness for processed size ${width}x${height}")
            return averageBrightness

        } catch (e: OutOfMemoryError) {
            Timber.e(e, "OutOfMemoryError calculating exposure score even after potential resize. Processed bitmap: ${bitmapToProcess.width}x${bitmapToProcess.height}")
            return 0.0f
        } catch (e: Exception) {
            Timber.e(e, "Error calculating exposure score for processed bitmap: ${bitmapToProcess.width}x${bitmapToProcess.height}")
            return 0.0f
        } finally {
            resizedBitmap?.recycle() // 리사이징된 비트맵이 생성되었다면 명시적으로 재활용
            Timber.d("Recycled resizedBitmap for exposure score if it was created.")
        }
    }

    private fun convertToGrayscale(bmpOriginal: Bitmap): Bitmap {
        val height: Int = bmpOriginal.height
        val width: Int = bmpOriginal.width
        // ARGB_8888을 사용하지만, 실제로는 회색조로 만듭니다. 
        // Bitmap.Config.ALPHA_8 (그레이스케일 전용)도 고려할 수 있으나, 호환성 및 Canvas 처리 등을 위해 ARGB_8888 유지.
        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888) 
        val c = Canvas(bmpGrayscale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        c.drawBitmap(bmpOriginal, 0f, 0f, paint)
        return bmpGrayscale
    }
}
