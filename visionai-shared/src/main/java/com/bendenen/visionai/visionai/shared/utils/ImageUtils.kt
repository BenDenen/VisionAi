package com.bendenen.visionai.visionai.shared.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ThumbnailUtils
import android.util.Log
import java.nio.ByteBuffer

fun getTransformationMatrix(
    srcWidth: Int,
    srcHeight: Int,
    dstWidth: Int,
    dstHeight: Int,
    applyRotation: Int,
    maintainAspectRatio: Boolean
): Matrix {
    val matrix = Matrix()

    if (applyRotation != 0) {

        // Translate so center of image is at origin.
        matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f)

        // Rotate around origin.
        matrix.postRotate(applyRotation.toFloat())
    }

    // Account for the already applied rotation, if any, and then determine how
    // much scaling is needed for each axis.
    val transpose = (Math.abs(applyRotation) + 90) % 180 == 0

    val inWidth = if (transpose) srcHeight else srcWidth
    val inHeight = if (transpose) srcWidth else srcHeight

    // Apply scaling if necessary.
    if (inWidth != dstWidth || inHeight != dstHeight) {
        val scaleFactorX = dstWidth / inWidth.toFloat()
        val scaleFactorY = dstHeight / inHeight.toFloat()

        if (maintainAspectRatio) {
            // Scale by minimum factor so that dst is filled completely while
            // maintaining the aspect ratio. Some image may fall off the edge.
            val scaleFactor = Math.max(scaleFactorX, scaleFactorY)
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

fun getSquaredBitmap(bitmap: Bitmap): Bitmap {
    val dimension = bitmap.width.coerceAtMost(bitmap.height)
    return ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension)
}

fun Bitmap.toNormalizedFloatArray(): FloatArray {

    val intValues = IntArray(this.width * this.height)
    this.getPixels(intValues, 0, this.width, 0, 0, this.width, this.height)

    val channelNum = 3
    val rgbFloatArray = FloatArray(this.width * this.height * channelNum)

    for ((index, pixel) in intValues.withIndex()) {
        rgbFloatArray[index * channelNum + 0] = Color.red(pixel).toFloat() / 255
        rgbFloatArray[index * channelNum + 1] = Color.green(pixel).toFloat() / 255
        rgbFloatArray[index * channelNum + 2] = Color.blue(pixel).toFloat() / 255
    }
    return rgbFloatArray
}

fun Bitmap.toByteArray(): ByteArray {

    val intValues = IntArray(this.width * this.height)
    this.getPixels(intValues, 0, this.width, 0, 0, this.width, this.height)

    val channelNum = 4
    val byteArray = ByteArray(this.width * this.height * channelNum)

    for ((index, pixel) in intValues.withIndex()) {
        byteArray[index * channelNum + 0] = Color.alpha(pixel).toByte()
        byteArray[index * channelNum + 1] = Color.red(pixel).toByte()
        byteArray[index * channelNum + 2] = Color.green(pixel).toByte()
        byteArray[index * channelNum + 3] = Color.blue(pixel).toByte()
    }
    return byteArray
}

fun Bitmap.toThreeChannelByteArray(): ByteArray {

    val intValues = IntArray(this.width * this.height)
    this.getPixels(intValues, 0, this.width, 0, 0, this.width, this.height)

    val channelNum = 3
    val byteArray = ByteArray(this.width * this.height * channelNum)

    for ((index, pixel) in intValues.withIndex()) {
        byteArray[index * channelNum + 0] = Color.red(pixel).toByte()
        byteArray[index * channelNum + 1] = Color.green(pixel).toByte()
        byteArray[index * channelNum + 2] = Color.blue(pixel).toByte()
    }
    return byteArray
}

fun Bitmap.toNormalizedFloatByteBuffer(
    buffer: ByteBuffer,
    inputSize: Int,
    mean: Float
) {

    val intValues = IntArray(this.width * this.height)
    this.getPixels(intValues, 0, this.width, 0, 0, this.width, this.height)

    buffer.rewind()
    for (i in 0 until inputSize) {
        for (j in 0 until inputSize) {
            val pixelValue = intValues[i * inputSize + j]
            buffer.putFloat(((pixelValue shr 16 and 0xFF) / mean))
            buffer.putFloat(((pixelValue shr 8 and 0xFF) / mean))
            buffer.putFloat(((pixelValue and 0xFF) / mean))
        }
    }
}

@Synchronized fun ByteArray.toNormalizedFloatByteBuffer(
    buffer: ByteBuffer,
    mean: Float
) {
    buffer.rewind()
    this.forEach {
        buffer.putFloat(((2 * it.toPositiveInt()) / mean) - 1)
    }
}

fun ByteArray.toBitmap(
    width: Int,
    height: Int
): Bitmap {
    val pixelValues = IntArray(width * height)
    var intIndex = 0
    val channelNum = 4


    for ((index, pixel) in pixelValues.withIndex()) {
        val a = this[index * channelNum + 0].toPositiveInt()
        val r = this[index * channelNum + 1].toPositiveInt()
        val g = this[index * channelNum + 2].toPositiveInt()
        val b = this[index * channelNum + 3].toPositiveInt()
        pixelValues[intIndex++] = Color.argb(a, r, g, b)
    }
    return Bitmap.createBitmap(pixelValues, width, height, Bitmap.Config.ARGB_8888)
}

fun Byte.toPositiveInt() = toInt() and 0xFF

/**
 *
 * @param bmp input bitmap
 * @param contrast 0..10 1 is default
 * @param brightness -255..255 0 is default
 * @return new bitmap
 */
fun changeBitmapContrastBrightness(bmp: Bitmap, contrast: Float, brightness: Float): Bitmap {
    val cm = ColorMatrix(
        floatArrayOf(
            contrast,
            0f,
            0f,
            0f,
            brightness,
            0f,
            contrast,
            0f,
            0f,
            brightness,
            0f,
            0f,
            contrast,
            0f,
            brightness,
            0f,
            0f,
            0f,
            1f,
            0f
        )
    )
    val ret = Bitmap.createBitmap(bmp.width, bmp.height, bmp.config)
    val canvas = Canvas(ret)
    val paint = Paint()
    paint.colorFilter = ColorMatrixColorFilter(cm)
    canvas.drawBitmap(bmp, Matrix(), paint)
    return ret
}