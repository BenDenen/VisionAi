package com.bendenen.visionai

import android.app.Application
import android.net.Uri
import com.bendenen.visionai.outputencoder.OutputEncoder
import com.bendenen.visionai.videosource.VideoSource
import java.io.File

data class VisionAiConfig(
    val application: Application? = null,
    val videoUri: Uri? = null,
    val videoSource: VideoSource? = null,
    val outputEncoder: OutputEncoder? = null,
    val outputFile: File
)