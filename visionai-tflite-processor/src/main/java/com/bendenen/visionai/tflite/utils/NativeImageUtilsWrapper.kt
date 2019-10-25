package com.bendenen.visionai.tflite.utils

object NativeImageUtilsWrapper {

        init {
            System.loadLibrary("image-utils")
        }

    external fun resizeImage(
        imageByteArray: ByteArray,
        imageWidth: Int,
        imageHeight: Int,
        newWidth: Int,
        newHeight: Int,
        outputArray: ByteArray
    )

    external fun resizeAndNormalizeImage(
        imageByteArray: ByteArray,
        imageWidth: Int,
        imageHeight: Int,
        newWidth: Int,
        newHeight: Int,
        outputArray: FloatArray
    )
}