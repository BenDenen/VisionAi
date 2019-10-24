package com.bendenen.visionai.videosource.render

import android.view.Surface

abstract class ImageRender {

    var renderActionsListener: RenderActionsListener? = null

    abstract fun getSurface(): Surface

    abstract fun release()
}