package com.bendenen.visionai.example.screens.artisticstyletransfer.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlendMode
import android.net.Uri
import android.os.Environment
import com.bendenen.visionai.example.utils.VisionAiManager
import com.bendenen.visionai.tflite.styletransfer.step.ArtisticStyleTransferVideoProcessorStep
import com.bendenen.visionai.tflite.styletransfer.step.SourcesOrder
import com.bendenen.visionai.tflite.styletransfer.step.Style
import java.io.File

interface ArtisticStyleTransferFunctionsUseCase {

    suspend fun initVisionAi(
        videoUri: Uri,
        outputFileName: String
    )

    suspend fun getPreview(): Bitmap

    suspend fun initStyle(style: Style)

    suspend fun setBlendMode(blendMode: BlendMode?)

    suspend fun setSourceOrder(sourceOrder: SourcesOrder)

    suspend fun setTransparency(alpha: Int)

    class Impl(
        private val context: Context,
        private val visionAiManager: VisionAiManager
    ) : ArtisticStyleTransferFunctionsUseCase {

        private var styleTransferConfig: ArtisticStyleTransferVideoProcessorStep.StyleTransferConfig =
            ArtisticStyleTransferVideoProcessorStep.StyleTransferConfig(
                context,
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

        override suspend fun initStyle(style: Style) {
            styleTransferConfig = styleTransferConfig.copy(style = style)
            visionAiManager.initSteps(
                listOf(
                    ArtisticStyleTransferVideoProcessorStep(
                        styleTransferConfig
                    )
                )
            )
        }

        override suspend fun setBlendMode(blendMode: BlendMode?) {
            styleTransferConfig = styleTransferConfig.copy(blendMode = blendMode)
            visionAiManager.updateConfig(
                0, styleTransferConfig
            )
        }

        override suspend fun setSourceOrder(sourceOrder: SourcesOrder) {
            styleTransferConfig = styleTransferConfig.copy(sourcesOrder = sourceOrder)
            visionAiManager.updateConfig(
                0, styleTransferConfig
            )
        }

        override suspend fun setTransparency(alpha: Int) {
            styleTransferConfig = styleTransferConfig.copy(maskAlpha = alpha)
            visionAiManager.updateConfig(
                0, styleTransferConfig
            )
        }
    }
}