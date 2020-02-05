package com.bendenen.visionai.tflite.bodysegmentation.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.bendenen.visionai.tflite.bodysegmentation.step.BodySegmentationVideoProcessorStep.Companion.NULL_CLASS_VALUE
import com.bendenen.visionai.tflite.bodysegmentation.step.SegmentationMode
import com.bendenen.visionai.visionai.shared.utils.toPositiveInt

internal fun Array<Array<Array<FloatArray>>>.argMaxForModesWithConfidence(
    modeList: List<SegmentationMode>,
    minConfidence: Float = 0.0f
): Array<Array<ByteArray>> {
    require(this.size == 1) { "The first dimension of Array is too big ${this.size}" }

    val maskContentArray = this[0]
    val width = maskContentArray.size
    val height = maskContentArray[0].size
    val classes = maskContentArray[0][0].size
    var holder = 0f
    var indexHolder = 0

    val outputArray = Array(width) {
        Array(height) {
            ByteArray(
                1
            )
        }
    }

    val modeListIndexes = modeList.map { it.classIndex }

    for (i in 0 until width) {
        for (j in 0 until height) {
            for (c in 0 until classes) {

                val value = maskContentArray[i][j][c]
                if (c == 0 || value > holder) {
                    outputArray[i][j][0] = c.toByte()
                    holder = value
                    indexHolder = c
                }
                if (!modeListIndexes.contains(indexHolder) || holder < minConfidence) {
                    outputArray[i][j][0] = NULL_CLASS_VALUE
                    continue
                }
            }
        }
    }

    return outputArray
}

internal fun getMaskedImage(
    segmentationMask: Array<Array<ByteArray>>,
    imageByteArray: ByteArray
): Bitmap {

    val width = segmentationMask.size
    val height = segmentationMask[0].size

    val pixelArray = IntArray(width * height)

    var bitmapArrayIndex = 0
    var pixelIndex = 0

    for (i in 0 until width) {
        for (j in 0 until height) {
            val maskValue = segmentationMask[i][j][0]
            if (maskValue == NULL_CLASS_VALUE) {
                pixelArray[pixelIndex++] = Color.argb(0, 0, 0, 0)
                bitmapArrayIndex += 3
            } else {
                val r = imageByteArray[bitmapArrayIndex++].toPositiveInt()
                val g = imageByteArray[bitmapArrayIndex++].toPositiveInt()
                val b = imageByteArray[bitmapArrayIndex++].toPositiveInt()

                pixelArray[pixelIndex++] = Color.rgb(r, g, b)
            }
        }
    }
    return Bitmap.createBitmap(pixelArray, width, height, Bitmap.Config.ARGB_8888)
}