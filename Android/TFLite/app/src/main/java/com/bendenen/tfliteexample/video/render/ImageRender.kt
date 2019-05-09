package com.bendenen.tfliteexample.video.render

import android.view.Surface

abstract class ImageRender {

    var renderActionsListener: RenderActionsListener? = null

    abstract fun getSurface(): Surface

    abstract fun release()

}