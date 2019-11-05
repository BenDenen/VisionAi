package com.bendenen.visionai.outputencoder.jcodec

import android.graphics.Bitmap
import android.util.Log
import com.bendenen.visionai.outputencoder.OutputEncoder
import org.jcodec.api.android.AndroidSequenceEncoder
import org.jcodec.common.io.FileChannelWrapper
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.Rational
import org.jcodec.scale.BitmapUtil
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class JCodecOutputEncoderImpl : OutputEncoder {

    private lateinit var outputFilePath: String

    private var currentState = OutputEncoder.EncoderState.NOT_INITIALIZED

    private lateinit var encoder: AndroidSequenceEncoder

    private lateinit var outChannel: FileChannelWrapper

    override fun getEncoderState(): OutputEncoder.EncoderState = currentState

    override fun initialize(
        outputFile: File,
        outputVideoWidth: Int,
        outputVideoHeight: Int
    ) {
        if (currentState == OutputEncoder.EncoderState.ENCODING) {
            Log.e(TAG, "Can not initialize Encoder when it is in ENCODING state. Finish encoding. ")
            return
        }
        try {
            outChannel = NIOUtils.writableFileChannel(outputFile.absolutePath)
            outputFilePath = outputFile.absolutePath
            encoder = AndroidSequenceEncoder(outChannel, Rational.R(30, 1))
        } catch (ex: FileNotFoundException) {
            Log.e(TAG, "Can not initialize Encoder. File not found:  ", ex)
            return
        } catch (ex: IOException) {
            Log.e(TAG, "Can not initialize.  ", ex)
            return
        }

        currentState = OutputEncoder.EncoderState.INITIALIZED
    }

    override fun getFilePath(): String {
        assert(::outputFilePath.isInitialized)
        return outputFilePath
    }

    override fun encodeBitmap(bitmap: Bitmap) {
        try {
            if (currentState != OutputEncoder.EncoderState.ENCODING) {
                currentState = OutputEncoder.EncoderState.ENCODING
            }
            Log.e("MyTag", "Start pic")
            val pic = BitmapUtil.fromBitmap(bitmap)
            Log.e("MyTag", "End pic")
            encoder.encodeNativeFrame(pic)
        } catch (ex: IOException) {
            Log.e(TAG, "Error during encoding process ", ex)
            finish()
        }
    }

    override fun finish() {
        try {
            encoder.finish()
        } catch (ex: IOException) {
            Log.e(TAG, "Error during encoding finishing ", ex)
        } finally {
            NIOUtils.closeQuietly(outChannel)
            currentState = OutputEncoder.EncoderState.FINISHED
        }
    }

    companion object {
        private val TAG = "JCodecOutputEncoderImpl"
    }
}