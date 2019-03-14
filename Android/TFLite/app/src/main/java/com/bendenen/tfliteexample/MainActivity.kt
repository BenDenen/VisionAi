package com.bendenen.tfliteexample

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bendenen.tfliteexample.video.VideoSource
import com.bendenen.tfliteexample.video.VideoSourceListener
import com.bendenen.tfliteexample.video.camera2.Camera2VideoSourceImpl
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), VideoSourceListener {

    private lateinit var videoSource: VideoSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        videoSource = Camera2VideoSourceImpl(application, FRAME_WIDTH, FRAME_HEIGHT)

        if (!allPermissionsGranted() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(getRequiredPermissions(), PERMISSIONS_REQUEST_CODE)
            return
        }
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) {
            videoSource.useBitmap(true)
            videoSource.attach(this)
        }
    }

    override fun onPause() {
        if (allPermissionsGranted()) {
            videoSource.detach()
        }
        super.onPause()
    }

    override fun onNewFrame(rgbBytes: ByteArray) {
        // TODO: Pass to ML Interface
    }

    override fun onNewBitmap(bitmap: Bitmap) {
        runOnUiThread {
            input_surface.setImageBitmap(bitmap)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun getRequiredPermissions(): Array<String> {
        return try {
            val info = packageManager?.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val ps = info!!.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                emptyArray()
            }
        } catch (e: Exception) {
            emptyArray()
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 1

        // Work resolution
        private const val FRAME_WIDTH = 640
        private const val FRAME_HEIGHT = 480
    }
}
