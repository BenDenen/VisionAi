package com.bendenen.visionai.example.screens.bodysegmentation.usecase

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import com.bendenen.visionai.example.utils.VisionAiManager
import com.bendenen.visionai.tflite.bodysegmentation.step.BodySegmentationConfig
import com.bendenen.visionai.tflite.bodysegmentation.step.BodySegmentationVideoProcessorStep
import com.bendenen.visionai.tflite.bodysegmentation.step.SegmentationMode
import java.io.File

interface BodySegmentationFunctionUseCase {

    suspend fun initVisionAi(
        videoUri: Uri,
        outputFileName: String
    )

    suspend fun initWithModes(modes: List<SegmentationMode>)

    suspend fun getPreview(): Bitmap

    class Impl(
        private val context: Context,
        private val visionAiManager: VisionAiManager
    ) : BodySegmentationFunctionUseCase {

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

        override suspend fun initWithModes(modes: List<SegmentationMode>) {
            visionAiManager.initSteps(
                listOf(
                    BodySegmentationVideoProcessorStep(
                        BodySegmentationConfig(context, modes)
                    )
                )
            )
        }

        override suspend fun getPreview(): Bitmap =
            visionAiManager.getPreview(1000).getBitmapForNextStep()
    }
}