package com.bendenen.visionai.example.screens.artisticstyletransfer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bendenen.visionai.example.R
import com.bendenen.visionai.example.screens.artisticstyletransfer.adapters.BlendModeAdapter
import com.bendenen.visionai.example.screens.artisticstyletransfer.adapters.StyleListAdapter
import com.bendenen.visionai.example.screens.artisticstyletransfer.viewmodel.ArtisticStyleTransferViewModel
import kotlinx.android.synthetic.main.activity_artistic_style_transfer.*
import org.koin.androidx.scope.currentScope

class ArtisticStyleTransferActivity : AppCompatActivity() {

    private val viewModel by currentScope.inject<ArtisticStyleTransferViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artistic_style_transfer)

        val styleListAdapter = StyleListAdapter(viewModel)
        style_list.adapter = styleListAdapter

        val blendModeAdapter = BlendModeAdapter(viewModel)
        blend_mode_list.adapter = blendModeAdapter

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
        viewModel.styleList.observe(this, Observer { styles ->
            styleListAdapter.submitList(styles)
        })
        viewModel.blendModeList.observe(this, Observer {
            blendModeAdapter.submitList(it)
        })
        viewModel.isVideoLoaded.observe(this, Observer {
            style_stub.visibility = if (it) {
                style_list.alpha = 1.0f
                View.GONE
            } else {
                style_list.alpha = 0.6f
                View.VISIBLE
            }
        })
        viewModel.isStyleSelected.observe(this, Observer {
            blend_mode_stub.visibility = if (it) {
                blend_mode_list.alpha = 1.0f
                View.GONE
            } else {
                blend_mode_list.alpha = 0.6f
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
            Intent(context, ArtisticStyleTransferActivity::class.java)
    }
}