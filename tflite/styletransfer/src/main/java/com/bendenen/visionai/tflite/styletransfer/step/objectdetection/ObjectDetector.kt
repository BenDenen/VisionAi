package com.bendenen.visionai.tflite.styletransfer.step.objectdetection

import android.graphics.Bitmap
import android.graphics.RectF

interface ObjectDetector {

    val statString: String

    fun recognizeImage(bitmap: Bitmap): List<Recognition>

    fun recognizeImageBytes(bytes: ByteArray): List<Recognition>

    fun enableStatLogging(debug: Boolean)

    fun close()

    fun setNumThreads(num_threads: Int)

    fun setUseNNAPI(isChecked: Boolean)

    data class Recognition(
        val id: String,
        val title: String,
        val confidence: Float,
        private var location: RectF
    ) {

        fun getLocation(): RectF {
            return RectF(location)
        }

        fun setLocation(location: RectF) {
            this.location = location
        }
    }
}
