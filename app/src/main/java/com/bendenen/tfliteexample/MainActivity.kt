package com.bendenen.tfliteexample

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bendenen.tfliteexample.selectstyle.SelectStyleActivity
import com.bendenen.visionai.VisionAi
import com.bendenen.visionai.VisionAiConfig
import com.bendenen.visionai.tflite.styletransfer.step.ArtisticStyleTransferStep
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), VisionAi.ResultListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!allPermissionsGranted() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(getRequiredPermissions(), PERMISSIONS_REQUEST_CODE)
            return
        }

        select_style_effect.setOnClickListener {
            startActivityForResult(
                SelectStyleActivity.getStartIntent(this),
                REQUEST_SELECT_STYLE_CODE
            )
        }

        record_video_button.setOnClickListener {
            Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
                takeVideoIntent.resolveActivity(packageManager)?.also {
                    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE_CODE)
                }
            }
        }
        select_video_button.setOnClickListener {
            val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )

            startActivityForResult(galleryIntent, REQUEST_VIDEO_CAPTURE_CODE)
        }
        request_preview.setOnClickListener {
            loading_indicator.visibility = View.VISIBLE
            VisionAi.requestPreview(1000) {
                input_surface.setImageBitmap(it)
                loading_indicator.visibility = View.GONE
            }
        }
        request_processing.setOnClickListener {
            video_processing_progress.visibility = View.VISIBLE
            request_preview.isEnabled = false
            VisionAi.start(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_VIDEO_CAPTURE_CODE && resultCode == RESULT_OK) {
            VisionAi.stop()
            data?.data?.let {
                if (allPermissionsGranted()) {
                    loading_indicator.visibility = View.VISIBLE
                    VisionAi.init(
                        VisionAiConfig(
                            application = application,
                            videoUri = it
                        )
                    ) {
                        loading_indicator.visibility = View.GONE
                        request_preview.isEnabled = true
                        request_processing.isEnabled = true
                    }
                }
            }

            return
        }
        if (requestCode == REQUEST_SELECT_STYLE_CODE && resultCode == RESULT_OK) {

            data?.let {
                val style = SelectStyleActivity.getStyleFromData(data)
                style?.let {
                    VisionAi.setSteps(
                        listOf(
                            ArtisticStyleTransferStep(
                                this,
                                style
                            )
                        )

                    )
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        VisionAi.stop()
        super.onPause()
    }

    override fun onFrameResult(bitmap: Bitmap) {
        input_surface.setImageBitmap(bitmap)
    }

    override fun onFileResult(filePath: String) {
        request_preview.isEnabled = true
        video_processing_progress.visibility = View.GONE
        play_button.visibility = View.VISIBLE

        play_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(filePath))
            intent.setDataAndType(Uri.parse(filePath), "video/mp4")
            startActivity(intent)
        }
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

        private const val REQUEST_VIDEO_CAPTURE_CODE = 11
        private const val REQUEST_SELECT_STYLE_CODE = 12

        // Work resolution
        private const val FRAME_WIDTH = 640
        private const val FRAME_HEIGHT = 480
    }
}
