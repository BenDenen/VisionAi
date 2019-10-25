package com.bendenen.visionai.tflite.utils

import android.content.res.AssetManager
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/** Memory-map the model file in Assets.  */
@Throws(IOException::class)
fun AssetManager.loadModelFile(modelFilename: String): MappedByteBuffer {
    val fileDescriptor = this.openFd(modelFilename)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
}