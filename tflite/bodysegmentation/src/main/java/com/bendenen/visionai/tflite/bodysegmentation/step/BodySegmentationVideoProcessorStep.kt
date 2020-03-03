package com.bendenen.visionai.tflite.bodysegmentation.step

import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import com.bendenen.visionai.tflite.bodysegmentation.BodySegmentationMlExecutor
import com.bendenen.visionai.tflite.bodysegmentation.utils.argMaxForModesWithConfidence
import com.bendenen.visionai.tflite.bodysegmentation.utils.getMaskedImage
import com.bendenen.visionai.videoprocessor.VideoProcessorStep
import com.bendenen.visionai.visionai.shared.utils.getTransformationMatrix
import com.bendenen.visionai.visionai.shared.utils.toThreeChannelByteArray

class BodySegmentationVideoProcessorStep(
    config: BodySegmentationConfig
) : VideoProcessorStep<BodySegmentationConfig, BodySegmentationResult>(config) {

    private lateinit var bodySegmentationMlExecutor: BodySegmentationMlExecutor
    private lateinit var croppedImage: Bitmap

    private lateinit var finalImage: Bitmap
    private lateinit var frameToCropTransform: Matrix
    private lateinit var cropToFrameTransform: Matrix

    override fun getWidthForNextStep(): Int {
        assert(::finalImage.isInitialized)
        return finalImage.width
    }

    override fun getHeightForNextStep(): Int {
        assert(::finalImage.isInitialized)
        return finalImage.height
    }

    override suspend fun initWithConfig() {
        bodySegmentationMlExecutor = BodySegmentationMlExecutor.Impl(stepConfig.context)
        croppedImage = Bitmap.createBitmap(
            bodySegmentationMlExecutor.getContentImageSize(),
            bodySegmentationMlExecutor.getContentImageSize(),
            Bitmap.Config.ARGB_8888
        )
        finalImage = Bitmap.createBitmap(
            stepConfig.videoSourceWidth,
            stepConfig.videoSourceHeight,
            Bitmap.Config.ARGB_8888
        )
        frameToCropTransform = getTransformationMatrix(
            stepConfig.videoSourceWidth,
            stepConfig.videoSourceHeight,
            bodySegmentationMlExecutor.getContentImageSize(),
            bodySegmentationMlExecutor.getContentImageSize(),
            0,
            false
        )
        cropToFrameTransform = Matrix().also {
            frameToCropTransform.invert(it)
        }
    }

    override suspend fun updateWithConfig(newConfig: BodySegmentationConfig) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun applyForData(bitmap: Bitmap): BodySegmentationResult {

        val croppedCanvas = Canvas(croppedImage)
        croppedCanvas.drawBitmap(bitmap, frameToCropTransform, null)

        val croppedImageByteArray = croppedImage.toThreeChannelByteArray()

        val result = bodySegmentationMlExecutor.segment(
            croppedImageByteArray,
            croppedImage.width,
            croppedImage.height
        )

        val segmentationMask = result.argMaxForModesWithConfidence(stepConfig.segmentationModes, MIN_CONFIDENCE)

        val maskedImage = getMaskedImage(segmentationMask, croppedImageByteArray)

        val finalBitmapCanvas = Canvas(finalImage)

        finalBitmapCanvas.drawBitmap(bitmap, Matrix(), null)
        finalBitmapCanvas.drawBitmap(maskedImage, cropToFrameTransform, Paint().also {
            it.blendMode = BlendMode.DST_IN
        })

        return BodySegmentationResult(bitmap, segmentationMask, finalImage)
    }

    companion object {
        const val NULL_CLASS_VALUE = (-1).toByte()
        const val MIN_CONFIDENCE = 0f
    }
}