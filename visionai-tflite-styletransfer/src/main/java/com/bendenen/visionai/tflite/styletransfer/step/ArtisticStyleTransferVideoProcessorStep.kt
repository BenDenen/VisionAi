package com.bendenen.visionai.tflite.styletransfer.step

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import com.bendenen.visionai.tflite.styletransfer.ArtisticStyleTransfer
import com.bendenen.visionai.tflite.styletransfer.ArtisticStyleTransferImpl
import com.bendenen.visionai.videoprocessor.StepConfig
import com.bendenen.visionai.videoprocessor.VideoProcessorStep
import com.bendenen.visionai.visionai.shared.utils.getTransformationMatrix
import com.bendenen.visionai.visionai.shared.utils.toBitmap
import com.bendenen.visionai.visionai.shared.utils.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArtisticStyleTransferVideoProcessorStep(
    styleTransferConfig: StyleTransferConfig
) : VideoProcessorStep<ArtisticStyleTransferVideoProcessorStep.StyleTransferConfig>(
    styleTransferConfig
) {

    private lateinit var artisticStyleTransfer: ArtisticStyleTransfer
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

        artisticStyleTransfer = ArtisticStyleTransferImpl(
            stepConfig.context
        )
        croppedImage = Bitmap.createBitmap(
            artisticStyleTransfer.getContentImageSize(),
            artisticStyleTransfer.getContentImageSize(),
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


        if ((styleBitmap.width != artisticStyleTransfer.getStyleImageSize())
            || (styleBitmap.height != artisticStyleTransfer.getStyleImageSize())
        ) {
            val transform = getTransformationMatrix(
                styleBitmap.width,
                styleBitmap.height,
                artisticStyleTransfer.getContentImageSize(),
                artisticStyleTransfer.getContentImageSize(),
                0,
                false
            )

            val finalBitmap = Bitmap.createBitmap(
                artisticStyleTransfer.getStyleImageSize(),
                artisticStyleTransfer.getStyleImageSize(),
                Bitmap.Config.ARGB_8888
            )

            val croppedCanvas = Canvas(finalBitmap)
            croppedCanvas.drawBitmap(styleBitmap, transform, null)
            artisticStyleTransfer.setStyle(finalBitmap)
        } else {
            artisticStyleTransfer.setStyle(styleBitmap)
        }

        finalImage = Bitmap.createBitmap(
            stepConfig.videoSourceWidth,
            stepConfig.videoSourceHeight,
            Bitmap.Config.ARGB_8888
        )
        frameToCropTransform = getTransformationMatrix(
            stepConfig.videoSourceWidth,
            stepConfig.videoSourceHeight,
            artisticStyleTransfer.getContentImageSize(),
            artisticStyleTransfer.getContentImageSize(),
            0,
            false
        )
        cropToFrameTransform = Matrix().also {
            frameToCropTransform.invert(it)
        }
    }

    override suspend fun updateWithConfig(newConfig: StyleTransferConfig) {
        if(stepConfig.style != newConfig.style) {
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

    override fun applyForData(bitmap: Bitmap): Bitmap {
        val croppedCanvas = Canvas(croppedImage)
        croppedCanvas.drawBitmap(bitmap, frameToCropTransform, null)

        val styledBitmap =
            applyForData(croppedImage.toByteArray(), croppedImage.width, croppedImage.height)

        val finalBitmapCanvas = Canvas(finalImage)

        val blendMode = stepConfig.blendMode
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

        return finalImage
    }

    private fun applyForData(rgbBytes: ByteArray, width: Int, height: Int): Bitmap {
        val result = artisticStyleTransfer.styleTransform(
            rgbBytes,
            width,
            height
        )
        return result.toBitmap()
    }

    data class StyleTransferConfig(
        var context: Context,
        var style: Style,
        var blendMode: BlendMode? = null,
        var sourcesOrder: SourcesOrder = SourcesOrder.FORWARD,
        var maskAlpha: Int = 255

    ) : StepConfig()


}