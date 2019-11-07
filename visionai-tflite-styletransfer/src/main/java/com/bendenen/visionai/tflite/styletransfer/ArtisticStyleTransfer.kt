package com.bendenen.visionai.tflite.styletransfer

import androidx.annotation.WorkerThread

interface ArtisticStyleTransfer {

    fun setStyle(style: Style)

    @WorkerThread
    fun styleTransform(
        contentImageData: ByteArray,
        imageWidth: Int,
        imageHeight: Int
    ): Array<Array<Array<FloatArray>>>

    fun close()
}