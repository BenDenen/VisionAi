package com.bendenen.visionai.utils

object NativeImageUtilsWrapper {

        init {
            System.loadLibrary("image-utils")
        }

    external fun resizeImage(
        imageByteArray:ByteArray,
        imageWidth:Int,
        imageHeight:Int,
        newWidth:Int,
        newHeight:Int,
        outputArray:ByteArray
    )
}