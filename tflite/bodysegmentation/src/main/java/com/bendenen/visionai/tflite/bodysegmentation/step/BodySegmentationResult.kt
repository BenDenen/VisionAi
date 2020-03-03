package com.bendenen.visionai.tflite.bodysegmentation.step

import android.graphics.Bitmap
import com.bendenen.visionai.videoprocessor.StepResult

class BodySegmentationResult(
    private val source: Bitmap,
    private val result: Array<Array<ByteArray>>,
    private val maskedImage:Bitmap
) : StepResult<Array<Array<ByteArray>>> {
    override fun getSourceBitmap(): Bitmap = source

    override fun getResult(): Array<Array<ByteArray>> = result

    override fun getBitmapForNextStep(): Bitmap = maskedImage
}