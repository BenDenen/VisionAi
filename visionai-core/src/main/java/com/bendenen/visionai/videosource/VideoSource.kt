package com.bendenen.visionai.videosource

interface VideoSource {

    fun getSourceWidth(): Int

    fun getSourceHeight(): Int

    fun isAttached(): Boolean

    fun attach(videoSourceListener: VideoSourceListener)

    fun useBitmap(useBitmap: Boolean)

    fun detach()

    fun release()
}
