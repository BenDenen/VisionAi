package com.bendenen.visionai.outputencoder

import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import java.io.File

interface OutputEncoder {

    fun getEncoderState(): EncoderState

    @WorkerThread
    fun encodeBitmap(bitmap: Bitmap)

    fun initialize(
        outputFile: File,
        outputVideoWidth: Int,
        outputVideoHeight: Int
    )

    fun finish()

    enum class EncoderState {
        NOT_INITIALIZED,
        INITIALIZED,
        ENCODING,
        FINISHED
    }
}