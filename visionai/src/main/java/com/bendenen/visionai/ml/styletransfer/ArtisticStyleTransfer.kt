package com.bendenen.visionai.ml.styletransfer

import androidx.annotation.WorkerThread

interface ArtisticStyleTransfer {

    fun setStyleImage(styleImagePath: String)

    @WorkerThread
    fun styleTransform(
        contentImageData: ByteArray,
        imageWidth: Int,
        imageHeight: Int
    ): Array<Array<Array<FloatArray>>>

    fun close()
}