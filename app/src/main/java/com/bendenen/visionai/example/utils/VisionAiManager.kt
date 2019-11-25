package com.bendenen.visionai.example.utils

import android.app.Application
import android.net.Uri
import com.bendenen.visionai.VisionAi
import com.bendenen.visionai.VisionAiConfig
import com.bendenen.visionai.videoprocessor.StepConfig
import com.bendenen.visionai.videoprocessor.StepResult
import com.bendenen.visionai.videoprocessor.VideoProcessorStep
import java.io.File

interface VisionAiManager {

    suspend fun init(
        videoUri: Uri,
        outputFile: File
    )

    fun stop()

    fun start(resultListener: VisionAi.ResultListener)

    suspend fun getPreview(timestamp: Long): StepResult<out Any>

    suspend fun <C : StepConfig, R : StepResult<out Any>> initSteps(videoProcessorSteps: List<VideoProcessorStep<C, R>>)

    suspend fun updateConfig(stepIndex: Int, config: StepConfig)

    class Impl(
        private val application: Application
    ) : VisionAiManager {

        override suspend fun init(videoUri: Uri, outputFile: File) {
            VisionAi.init(
                VisionAiConfig(
                    application = application,
                    videoUri = videoUri,
                    outputFile = outputFile
                )
            )
        }

        override fun stop() {
            VisionAi.stop()
        }

        override fun start(resultListener: VisionAi.ResultListener) {
            VisionAi.start(resultListener)
        }

        override suspend fun getPreview(timestamp: Long): StepResult<out Any> =
            VisionAi.requestPreview(timestamp)

        override suspend fun <C : StepConfig, R : StepResult<out Any>> initSteps(
            videoProcessorSteps: List<VideoProcessorStep<C, R>>
        ) {
            VisionAi.initSteps(videoProcessorSteps)
        }

        override suspend fun updateConfig(stepIndex: Int, config: StepConfig) {
            VisionAi.updateConfigForStep(stepIndex, config)
        }
    }
}