package com.bendenen.visionai.videosource

import android.net.Uri

interface VideoSource {

    fun loadVideoFile(uri: Uri, ready: () -> Unit)

    fun getSourceWidth(): Int

    fun getSourceHeight(): Int

    fun isAttached(): Boolean

    fun attach(videoSourceListener: VideoSourceListener)

    fun useBitmap(useBitmap: Boolean)

    fun detach()

    fun release()
}
