package com.bendenen.tfliteexample.video.camera2.render

import android.view.Surface

abstract class CameraRender {

    var renderActionsListener: RenderActionsListener? = null

    abstract fun getSurface(): Surface

    abstract fun release()

}