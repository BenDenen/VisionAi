package com.bendenen.visionai.example.screens.bodysegmentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bendenen.visionai.example.R
import com.bendenen.visionai.example.screens.bodysegmentation.viewmodel.BodySegmentationViewModel
import kotlinx.android.synthetic.main.activity_body_segmentation.*
import org.koin.androidx.scope.currentScope

class BodySegmentationActivity : AppCompatActivity() {

    private val viewModel by currentScope.inject<BodySegmentationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_body_segmentation)

        request_video.setOnClickListener {
            viewModel.requestVideo()
        }

        viewModel.previewImage.observe(this, Observer {
            input_surface.setImageBitmap(it)
        })

        viewModel.requestVideoEvent.observe(this, Observer {
            val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(
                galleryIntent,
                REQUEST_VIDEO_CAPTURE_CODE
            )
        })
        viewModel.isLoading.observe(this, Observer {
            loading_indicator.visibility = if (it) {
                if (request_video.visibility == View.VISIBLE) request_video.visibility = View.GONE
                View.VISIBLE
            } else View.GONE
        })

        viewModel.isVideoLoaded.observe(this, Observer {
            modes_stub.visibility = if (it) {
                modes_list.alpha = 1.0f
                View.GONE
            } else {
                modes_stub.alpha = 0.6f
                View.VISIBLE
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_VIDEO_CAPTURE_CODE && resultCode == RESULT_OK) {
            data?.data?.let {
                viewModel.initWithVideoPath(it)
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {

        private const val REQUEST_VIDEO_CAPTURE_CODE = 11

        fun getStartIntent(context: Context): Intent =
            Intent(context, BodySegmentationActivity::class.java)
    }
}