package com.bendenen.visionai.example.screens.artisticstyletransfer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.ArtisticStyleTransferLayout
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.styles.StyleImageLoader
import com.bendenen.visionai.example.screens.artisticstyletransfer.viewmodel.ArtisticStyleTransferViewModel
import com.bendenen.visionai.example.ui.AppTheme
import com.bendenen.visionai.tflite.styletransfer.step.Style
import org.koin.androidx.scope.currentScope

class ArtisticStyleTransferActivity : AppCompatActivity() {

    private val viewModel by currentScope.inject<ArtisticStyleTransferViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                ArtisticStyleTransferLayout(
                    liveDataState = viewModel.state,
                    handler = viewModel.handler,
                    styleImageLoader = currentScope.inject<StyleImageLoader>().value
                )
            }
        }

        viewModel.requestVideoEvent.observe(this, Observer {
            val videoIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(
                videoIntent,
                REQUEST_VIDEO_CAPTURE_CODE
            )
        })
        viewModel.addNewStyleEvent.observe(this, Observer {
            val pictureIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(
                pictureIntent,
                REQUEST_PICTURE_CAPTURE_CODE
            )
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_VIDEO_CAPTURE_CODE && resultCode == RESULT_OK) {
            data?.data?.let {
                viewModel.initWithVideoPath(it)
            }
            return
        }
        if (requestCode == REQUEST_PICTURE_CAPTURE_CODE && resultCode == RESULT_OK) {
            data?.data?.let {
                viewModel.addStyle(Style.PhotoUriStyle(it, it.toString().substringAfterLast("/")))
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {

        private const val REQUEST_VIDEO_CAPTURE_CODE = 11
        private const val REQUEST_PICTURE_CAPTURE_CODE = 12

        fun getStartIntent(context: Context): Intent =
            Intent(context, ArtisticStyleTransferActivity::class.java)
    }
}