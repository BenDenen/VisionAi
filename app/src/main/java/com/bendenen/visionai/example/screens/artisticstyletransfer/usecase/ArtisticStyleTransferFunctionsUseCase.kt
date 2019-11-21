package com.bendenen.visionai.example.screens.artisticstyletransfer.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlendMode
import android.net.Uri
import android.os.Environment
import com.bendenen.visionai.example.utils.VisionAiManager
import com.bendenen.visionai.tflite.styletransfer.step.ArtisticStyleTransferStep
import com.bendenen.visionai.tflite.styletransfer.step.SourcesOrder
import com.bendenen.visionai.tflite.styletransfer.step.Style
import java.io.File

interface ArtisticStyleTransferFunctionsUseCase {

    suspend fun initVisionAi(
        videoUri: Uri,
        outputFileName: String
    )

    suspend fun getPreview(): Bitmap

    fun setStyle(style: Style)

    fun setBlendMode(blendMode: BlendMode?)

    fun setSourceOrder(sourceOrder: SourcesOrder)

    fun setTransparency(alpha: Int)

    class Impl(
        private val context: Context,
        private val visionAiManager: VisionAiManager
    ) : ArtisticStyleTransferFunctionsUseCase {

        private var config: ArtisticStyleTransferStep.Config = ArtisticStyleTransferStep.Config(
            Style.AssetStyle("", "")
        )

        override suspend fun initVisionAi(videoUri: Uri, outputFileName: String) {
            visionAiManager.stop()
            visionAiManager.init(
                videoUri,
                File(
                    context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                    outputFileName
                )
            )
        }

        override suspend fun getPreview(): Bitmap =
            visionAiManager.getPreview(1000)

        override fun setStyle(style: Style) {
            config = config.copy(style = style)
            visionAiManager.setSteps(listOf(ArtisticStyleTransferStep(context, config)))
        }

        override fun setBlendMode(blendMode: BlendMode?) {
            config = config.copy(blendMode = blendMode)
            visionAiManager.setSteps(listOf(ArtisticStyleTransferStep(context, config)))
        }

        override fun setSourceOrder(sourceOrder: SourcesOrder) {
            config = config.copy(sourcesOrder = sourceOrder)
            visionAiManager.setSteps(listOf(ArtisticStyleTransferStep(context, config)))
        }

        override fun setTransparency(alpha: Int) {
            config = config.copy(maskAlpha = alpha)
            visionAiManager.setSteps(listOf(ArtisticStyleTransferStep(context, config)))
        }
    }
}