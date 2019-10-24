package com.bendenen.visionai.videoprocessor.outputencoder

import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import java.io.File

interface OutputEncoder {

    fun getEncoderState(): EncoderState

    @WorkerThread
    fun encodeBitmap(bitmap: Bitmap)

    fun initialize(outputFile: File)

    fun finish()

    enum class EncoderState {
        NOT_INITIALIZED,
        INITIALIZED,
        ENCODING,
        FINISHED
    }
}