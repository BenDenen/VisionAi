package com.bendenen.visionai.example.utils

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import com.bendenen.visionai.VisionAi
import com.bendenen.visionai.VisionAiConfig
import com.bendenen.visionai.videoprocessor.ProcessorStep
import java.io.File

interface VisionAiManager {

    suspend fun init(
        videoUri: Uri,
        outputFile: File
    )

    fun stop()

    fun start(resultListener: VisionAi.ResultListener)

    suspend fun getPreview(timestamp: Long): Bitmap

    fun setSteps(steps: List<ProcessorStep>)

    class Impl(
        val application: Application
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

        override suspend fun getPreview(timestamp: Long): Bitmap =
            VisionAi.requestPreview(timestamp)

        override fun setSteps(steps: List<ProcessorStep>) {
            VisionAi.setSteps(steps)
        }
    }
}