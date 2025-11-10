package com.bes2.ml

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import timber.log.Timber
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

object ImagePhashGenerator {

    private const val HASH_SIZE = 8 // 최종 해시의 한 변의 크기 (8x8 = 64비트 해시)
    private const val RESIZE_DIMENSION = 32 // DCT 적용 전 리사이즈 크기

    fun generatePhash(bitmap: Bitmap): String {
        Timber.d("Generating pHash for bitmap: ${bitmap.width}x${bitmap.height}")

        try {
            // 1. 이미지 리사이즈
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, RESIZE_DIMENSION, RESIZE_DIMENSION, true)
            Timber.d("Resized bitmap to ${resizedBitmap.width}x${resizedBitmap.height}")

            // 2. 그레이스케일 변환
            val grayscaleBitmap = convertToGrayscale(resizedBitmap)
            Timber.d("Converted to grayscale")

            // 3. DCT (Discrete Cosine Transform) 적용
            //    - 픽셀 값을 2D 배열로 추출
            val pixels = getPixelMatrix(grayscaleBitmap)
            //    - DCT 적용
            val dctCoefficients = applyDCT(pixels)
            Timber.d("Applied DCT")

            // 4. DCT 계수 중 좌상단 일부(HASH_SIZE x HASH_SIZE) 선택
            val topLeftDct = Array(HASH_SIZE) { DoubleArray(HASH_SIZE) }
            for (i in 0 until HASH_SIZE) {
                for (j in 0 until HASH_SIZE) {
                    topLeftDct[i][j] = dctCoefficients[i][j]
                }
            }
            Timber.d("Extracted top-left DCT coefficients")

            // 5. 선택된 계수들의 평균 계산
            var sum = 0.0
            for (i in 0 until HASH_SIZE) {
                for (j in 0 until HASH_SIZE) {
                    sum += topLeftDct[i][j]
                }
            }
            val meanDctValue = sum / (HASH_SIZE * HASH_SIZE)
            Timber.d("Calculated mean DCT value: $meanDctValue")

            // 6. 평균값과 비교하여 이진 해시 생성
            var hash = 0L
            for (i in 0 until HASH_SIZE) {
                for (j in 0 until HASH_SIZE) {
                    hash = hash shl 1
                    if (topLeftDct[i][j] > meanDctValue) {
                        hash = hash or 1L
                    }
                }
            }
            // Long.toHexString()을 사용하여 부호 없는 64비트 16진수 문자열로 변환
            val phashString = java.lang.Long.toHexString(hash).padStart(16, '0')
            Timber.i("Generated pHash: $phashString")
            return phashString

        } catch (e: Exception) {
            Timber.e(e, "Error during pHash generation")
            return "" // 오류 발생 시 빈 문자열 반환
        }
    }

    private fun convertToGrayscale(bmpOriginal: Bitmap): Bitmap {
        val height: Int = bmpOriginal.height
        val width: Int = bmpOriginal.width
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

    private fun getPixelMatrix(bitmap: Bitmap): Array<DoubleArray> {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = Array(height) { DoubleArray(width) }
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                pixels[y][x] = (pixel and 0xFF).toDouble()
            }
        }
        return pixels
    }

    // 2D DCT Type-II 구현
    private fun applyDCT(matrix: Array<DoubleArray>): Array<DoubleArray> {
        val N = matrix.size // 행의 수 (높이)
        val M = matrix[0].size // 열의 수 (너비)
        val dctMatrix = Array(N) { DoubleArray(M) }

        val c_u = DoubleArray(N) { if (it == 0) 1.0 / sqrt(N.toDouble()) else sqrt(2.0 / N) }
        val c_v = DoubleArray(M) { if (it == 0) 1.0 / sqrt(M.toDouble()) else sqrt(2.0 / M) }

        for (u in 0 until N) {
            for (v in 0 until M) {
                var sum = 0.0
                for (i in 0 until N) {
                    for (j in 0 until M) {
                        sum += matrix[i][j] *
                               cos((2 * i + 1) * u * PI / (2 * N)) *
                               cos((2 * j + 1) * v * PI / (2 * M))
                    }
                }
                dctMatrix[u][v] = c_u[u] * c_v[v] * sum
            }
        }
        return dctMatrix
    }

    fun calculateHammingDistance(phash1: String, phash2: String): Int {
        if (phash1.length != 16 || phash2.length != 16) {
            Timber.w("Cannot calculate Hamming distance: pHashes must be 16 hex characters long. phash1='$phash1', phash2='$phash2'")
            return -1
        }
        if (phash1.isEmpty() || phash2.isEmpty()) {
             Timber.w("Cannot calculate Hamming distance: Hashes cannot be empty. phash1='$phash1', phash2='$phash2'")
            return -1
        }

        try {
            val h1 = java.lang.Long.parseUnsignedLong(phash1, 16)
            val h2 = java.lang.Long.parseUnsignedLong(phash2, 16)

            var xorResult = h1 xor h2
            var distance = 0
            while (xorResult != 0L) {
                distance++
                xorResult = xorResult and (xorResult - 1)
            }
            Timber.d("Hamming distance between '$phash1' and '$phash2' is $distance")
            return distance
        } catch (e: NumberFormatException) {
            Timber.e(e, "Error converting hex pHash string to Long for Hamming distance: '$phash1', '$phash2'")
            return -1
        }
    }
}
