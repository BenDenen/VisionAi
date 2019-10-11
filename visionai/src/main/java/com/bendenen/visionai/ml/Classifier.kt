package com.bendenen.visionai.ml

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.annotation.WorkerThread

interface Classifier {

    val statString: String

    @WorkerThread
    fun recognizeImage(bitmap: Bitmap): List<Recognition>

    @WorkerThread
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
