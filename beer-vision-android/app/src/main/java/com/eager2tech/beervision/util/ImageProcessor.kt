package com.eager2tech.beervision.util

import android.graphics.Matrix
import android.util.Log
import kotlin.math.abs
import kotlin.math.max

object ImageProcessor {
    private const val kMaxChannelValue = 262143

    fun YUV2RGB(yV: Int, uV: Int, vV: Int): Int {
        // Adjust and check YUV values
        val y = (yV - 16).coerceAtLeast(0)
        val u = uV - 128
        val v = vV -128

        // This is the floating point equivalent. We do the conversion in integer
        // because some Android devices do not have floating point in hardware.
        // nR = (int)(1.164 * nY + 2.018 * nU);
        // nG = (int)(1.164 * nY - 0.813 * nV - 0.391 * nU);
        // nB = (int)(1.164 * nY + 1.596 * nV);
        val y1192 = 1192 * y
        var r = (y1192 + 1634 * v)
        var g = (y1192 - 833 * v - 400 * u)
        var b = (y1192 + 2066 * u)

        // Clipping RGB values to be inside boundaries [ 0 , kMaxChannelValue ]
        r = if (r > kMaxChannelValue) kMaxChannelValue else (if (r < 0) 0 else r)
        g = if (g > kMaxChannelValue) kMaxChannelValue else (if (g < 0) 0 else g)
        b = if (b > kMaxChannelValue) kMaxChannelValue else (if (b < 0) 0 else b)

        return -0x1000000 or ((r shl 6) and 0xff0000) or ((g shr 2) and 0xff00) or ((b shr 10) and 0xff)
    }

    fun YUV420ToARGB8888(yData: ByteArray, uData: ByteArray, vData: ByteArray, width: Int, height: Int, yRowStride: Int, uvRowStride: Int,
        uvPixelStride: Int, out: IntArray) {

        var yp = 0
        for (j in 0 until height) {
            val pY = yRowStride * j
            val pUV = uvRowStride * (j shr 1)

            for (i in 0 until width) {
                val uvOffset = pUV + (i shr 1) * uvPixelStride

                out[yp++] = YUV2RGB(
                    0xff and yData[pY + i].toInt(),
                    0xff and uData[uvOffset].toInt(),
                    0xff and vData[uvOffset].toInt()
                )
            }
        }
    }

    fun getTransformationMatrix(srcWidth: Int, srcHeight: Int,
        dstWidth: Int, dstHeight: Int,
        applyRotation: Int,
        maintainAspectRatio: Boolean
    ): Matrix {
        val matrix = Matrix()

        if (applyRotation != 0) {
            if (applyRotation % 90 != 0) {
                Log.e("Rotation", "Rotation != 90°, got: $applyRotation")
            }

            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f)

            // Rotate around origin.
            matrix.postRotate(applyRotation.toFloat())
        }

        // Account for the already applied rotation, if any, and then determine how
        // much scaling is needed for each axis.
        val transpose = (abs(applyRotation) + 90) % 180 == 0

        val inWidth = if (transpose) srcHeight else srcWidth
        val inHeight = if (transpose) srcWidth else srcHeight

        // Apply scaling if necessary.
        if (inWidth != dstWidth || inHeight != dstHeight) {
            val scaleFactorX = dstWidth / inWidth.toFloat()
            val scaleFactorY = dstHeight / inHeight.toFloat()

            if (maintainAspectRatio) {
                // Scale by minimum factor so that dst is filled completely while
                // maintaining the aspect ratio. Some image may fall off the edge.
                val scaleFactor = max(scaleFactorX.toDouble(), scaleFactorY.toDouble()).toFloat()
                matrix.postScale(scaleFactor, scaleFactor)
            } else {
                // Scale exactly to fill dst from src.
                matrix.postScale(scaleFactorX, scaleFactorY)
            }
        }

        if (applyRotation != 0) {
            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f)
        }

        return matrix
    }
}