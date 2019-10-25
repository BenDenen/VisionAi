package com.bendenen.tfliteexample

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bendenen.visionai.VisionAi
import com.bendenen.visionai.VisionAiConfig
import com.bendenen.visionai.tflite.videoprocessor.TfLiteVideoProcessorImpl
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), VisionAi.ResultListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!allPermissionsGranted() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(getRequiredPermissions(), PERMISSIONS_REQUEST_CODE)
            return
        }

        record_video_button.setOnClickListener {
            Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
                takeVideoIntent.resolveActivity(packageManager)?.also {
                    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
                }
            }
        }
        select_video_button.setOnClickListener {
            val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )

            startActivityForResult(galleryIntent, REQUEST_VIDEO_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            VisionAi.stop()
            data?.data?.let {
                if (allPermissionsGranted()) {
                    VisionAi.init(
                        VisionAiConfig(
                            application = application,
                            videoUri = it
                        )
                    ) {
                        VisionAi.setProcessor(TfLiteVideoProcessorImpl(application))
                        VisionAi.start(this)
                    }
                }
            }

            return
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        VisionAi.stop()
        super.onPause()
    }

    override fun onFrameResult(bitmap: Bitmap) {
        runOnUiThread {
            input_surface.setImageBitmap(bitmap)
        }
    }

    override fun onFileResult(filePath: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
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

        private const val REQUEST_VIDEO_CAPTURE = 11

        // Work resolution
        private const val FRAME_WIDTH = 640
        private const val FRAME_HEIGHT = 480
    }
}
