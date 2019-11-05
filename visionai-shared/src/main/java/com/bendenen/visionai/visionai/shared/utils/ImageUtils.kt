package com.bendenen.visionai.visionai.shared.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
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
        byteArray[index * channelNum + 0] = (pixel shr 16 and 0xFF).toByte()
        byteArray[index * channelNum + 1] = (pixel shr 8 and 0xFF).toByte()
        byteArray[index * channelNum + 2] = (pixel and 0xFF).toByte()
        byteArray[index * channelNum + 3] = Color.alpha(pixel).toByte()
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

@Throws(IllegalArgumentException::class)
fun Array<Array<Array<FloatArray>>>.toBitmap(): Bitmap {
    require(this.size == 1) { "The first dimension of Array is too big ${this.size}" }
    val bitmapContentArray = this[0]
    val width = bitmapContentArray.size
    val height = bitmapContentArray[0].size
    val channels = bitmapContentArray[0][0].size
    require(channels == 3) { "Channels should be equals 3 for RGB image but it is $channels" }
    val pixelValues = IntArray(width * height)
    var index = 0
    for (w in 0 until width) {
        for (h in 0 until height) {
            val r = (bitmapContentArray[w][h][0] * 255).toInt()
            val g = (bitmapContentArray[w][h][1] * 255).toInt()
            val b = (bitmapContentArray[w][h][2] * 255).toInt()
            pixelValues[index++] = Color.rgb(r, g, b)
        }
    }
    return Bitmap.createBitmap(pixelValues, width, height, Bitmap.Config.ARGB_8888)
}

fun ByteArray.toBitmap(
    width: Int,
    height: Int
) : Bitmap {
    val pixelValues = IntArray(width * height)
    var intIndex = 0
    val channelNum = 3


    for ((index, pixel) in pixelValues.withIndex()) {
        val r = this[index * channelNum + 0].toInt()
        val g = this[index * channelNum + 1].toInt()
        val b = this[index * channelNum + 2].toInt()
        pixelValues[intIndex++] = Color.rgb(r, g, b)
    }
    return Bitmap.createBitmap(pixelValues, width, height, Bitmap.Config.ARGB_8888)
}