package com.bendenen.visionai.tflite.styletransfer.step

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import com.bendenen.visionai.tflite.styletransfer.ArtisticStyleTransferMlExecutor
import com.bendenen.visionai.tflite.styletransfer.ArtisticStyleTransferMlExecutorImpl
import com.bendenen.visionai.tflite.styletransfer.utils.toBitmap
import com.bendenen.visionai.videoprocessor.VideoProcessorStep
import com.bendenen.visionai.visionai.shared.utils.getTransformationMatrix
import com.bendenen.visionai.visionai.shared.utils.toByteArray
import com.bendenen.visionai.visionai.shared.utils.toThreeChannelByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArtisticStyleTransferVideoProcessorStep(
    styleTransferConfig: StyleTransferConfig
) : VideoProcessorStep<StyleTransferConfig, ArtisticStyleTransferStepResult>(
    styleTransferConfig
) {

    private lateinit var artisticStyleTransferMlExecutor: ArtisticStyleTransferMlExecutor
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

    override suspend fun initWithConfig() = withContext(Dispatchers.IO) {

        artisticStyleTransferMlExecutor = ArtisticStyleTransferMlExecutorImpl(
            stepConfig.context
        )
        croppedImage = Bitmap.createBitmap(
            artisticStyleTransferMlExecutor.getContentImageSize(),
            artisticStyleTransferMlExecutor.getContentImageSize(),
            Bitmap.Config.ARGB_8888
        )
        val styleBitmap = when (val style = stepConfig.style) {
            is Style.AssetStyle -> {
                BitmapFactory.decodeStream(stepConfig.context.assets.open(style.styleFileName))
            }
            is Style.PhotoUriStyle -> {
                val inputStream =
                    stepConfig.context.contentResolver.openInputStream(style.styleFileUri)!!
                BitmapFactory.decodeStream(inputStream)
            }
            else -> throw IllegalArgumentException("Unknown Style type : ${stepConfig.style::class.java.simpleName}")
        }


        if ((styleBitmap.width != artisticStyleTransferMlExecutor.getStyleImageSize())
            || (styleBitmap.height != artisticStyleTransferMlExecutor.getStyleImageSize())
        ) {
            val transform = getTransformationMatrix(
                styleBitmap.width,
                styleBitmap.height,
                artisticStyleTransferMlExecutor.getContentImageSize(),
                artisticStyleTransferMlExecutor.getContentImageSize(),
                0,
                false
            )

            val finalBitmap = Bitmap.createBitmap(
                artisticStyleTransferMlExecutor.getStyleImageSize(),
                artisticStyleTransferMlExecutor.getStyleImageSize(),
                Bitmap.Config.ARGB_8888
            )

            val croppedCanvas = Canvas(finalBitmap)
            croppedCanvas.drawBitmap(styleBitmap, transform, null)
            artisticStyleTransferMlExecutor.setStyle(finalBitmap)
        } else {
            artisticStyleTransferMlExecutor.setStyle(styleBitmap)
        }

        finalImage = Bitmap.createBitmap(
            stepConfig.videoSourceWidth,
            stepConfig.videoSourceHeight,
            Bitmap.Config.ARGB_8888
        )
        frameToCropTransform = getTransformationMatrix(
            stepConfig.videoSourceWidth,
            stepConfig.videoSourceHeight,
            artisticStyleTransferMlExecutor.getContentImageSize(),
            artisticStyleTransferMlExecutor.getContentImageSize(),
            0,
            false
        )
        cropToFrameTransform = Matrix().also {
            frameToCropTransform.invert(it)
        }
    }

    override suspend fun updateWithConfig(newConfig: StyleTransferConfig) {
        if (stepConfig.style != newConfig.style) {
            stepConfig.apply {
                this.context = newConfig.context
                this.style = newConfig.style
                this.blendMode = newConfig.blendMode
                this.sourcesOrder = newConfig.sourcesOrder
                this.maskAlpha = newConfig.maskAlpha
            }
            initWithConfig()
            return
        }
        stepConfig.apply {
            this.context = newConfig.context
            this.style = newConfig.style
            this.blendMode = newConfig.blendMode
            this.sourcesOrder = newConfig.sourcesOrder
            this.maskAlpha = newConfig.maskAlpha
        }
    }

    override suspend fun applyForData(bitmap: Bitmap): ArtisticStyleTransferStepResult {
        val croppedCanvas = Canvas(croppedImage)
        croppedCanvas.drawBitmap(bitmap, frameToCropTransform, null)

        val styledBitmap =
            applyForData(croppedImage.toThreeChannelByteArray(), croppedImage.width, croppedImage.height)

        val finalBitmapCanvas = Canvas(finalImage)

        val blendMode = stepConfig.blendMode.blendMode
        if (blendMode != null) {
            when (stepConfig.sourcesOrder) {
                SourcesOrder.FORWARD -> {
                    finalBitmapCanvas.drawBitmap(bitmap, Matrix(), null)
                    finalBitmapCanvas.drawBitmap(styledBitmap, cropToFrameTransform, Paint().also {
                        it.blendMode = blendMode
                        it.alpha = stepConfig.maskAlpha
                    })
                }
                SourcesOrder.BACKWARD -> {
                    finalBitmapCanvas.drawBitmap(styledBitmap, cropToFrameTransform, null)
                    finalBitmapCanvas.drawBitmap(bitmap, Matrix(), Paint().also {
                        it.blendMode = blendMode
                        it.alpha = stepConfig.maskAlpha
                    })
                }
            }
        } else {
            finalBitmapCanvas.drawBitmap(styledBitmap, cropToFrameTransform, null)
        }

        return ArtisticStyleTransferStepResult(bitmap, styledBitmap, finalImage)
    }

    private fun applyForData(rgbBytes: ByteArray, width: Int, height: Int): Bitmap {
        val result = artisticStyleTransferMlExecutor.styleTransform(
            rgbBytes,
            width,
            height
        )
        return result.toBitmap()
    }
}