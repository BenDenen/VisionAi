package com.bendenen.visionai.videosource

import android.graphics.Bitmap
import android.net.Uri

interface MediaFileVideoSource : VideoSource {

    suspend fun loadVideoFile(uri: Uri)

    suspend fun requestPreview(timestamp: Long): Bitmap
}