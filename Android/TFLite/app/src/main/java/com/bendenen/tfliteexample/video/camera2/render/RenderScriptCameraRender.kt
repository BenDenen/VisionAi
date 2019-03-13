package com.bendenen.tfliteexample.video.camera2.render

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.renderscript.*
import android.util.Size
import android.view.Surface

internal class RenderScriptCameraRender(
    renderScript: RenderScript,
    private val previewSize: Size
) : CameraRender(), Allocation.OnBufferAvailableListener {

    override fun getSurface(): Surface = inputNormalAllocation.surface

    private val inputNormalAllocation: Allocation
    private val outputAllocation: Allocation
    private val scriptC = ScriptIntrinsicYuvToRGB.create(renderScript, Element.RGBA_8888(renderScript))
    private val rgbByteArray: ByteArray = ByteArray(previewSize.width * previewSize.height * 4)
    private lateinit var bitmap: Bitmap

    init {
        val yuvTypeBuilder = Type.Builder(renderScript, Element.YUV(renderScript))
        yuvTypeBuilder.setX(previewSize.width)
        yuvTypeBuilder.setY(previewSize.height)
        yuvTypeBuilder.setYuvFormat(ImageFormat.YUV_420_888)
        inputNormalAllocation = Allocation.createTyped(
            renderScript, yuvTypeBuilder.create(),
            Allocation.USAGE_IO_INPUT or Allocation.USAGE_SCRIPT
        )


        val rgbTypeBuilder = Type.Builder(renderScript, Element.RGBA_8888(renderScript))
        rgbTypeBuilder.setX(previewSize.width)
        rgbTypeBuilder.setY(previewSize.height)
        outputAllocation = Allocation.createTyped(
            renderScript, rgbTypeBuilder.create(),
            Allocation.USAGE_SCRIPT
        )

        scriptC.setInput(inputNormalAllocation)
        inputNormalAllocation.setOnBufferAvailableListener(this)
    }

    override fun onBufferAvailable(a: Allocation) {

        val listener = renderActionsListener ?: return

        // Get to newest input
        inputNormalAllocation.ioReceive()

        // Run processing pass
        scriptC.forEach(outputAllocation)

        outputAllocation.copyTo(rgbByteArray)
        listener.onNewRGBBytes(rgbByteArray)

        if (listener.useBitmap()) {
            if (!::bitmap.isInitialized) {
                bitmap = Bitmap.createBitmap(previewSize.width, previewSize.height, Bitmap.Config.ARGB_8888)
            }
            outputAllocation.copyTo(bitmap)
            listener.onNewBitmap(bitmap)
        }

    }

    override fun release() {
        inputNormalAllocation.surface.release()
        inputNormalAllocation.destroy()
        outputAllocation.destroy()
    }

}
