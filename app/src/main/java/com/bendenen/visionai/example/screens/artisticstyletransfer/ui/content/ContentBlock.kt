package com.bendenen.visionai.example.screens.artisticstyletransfer.ui.content

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bendenen.visionai.example.R
import com.bendenen.visionai.example.ui.themeTypography

sealed class ContentBlockState(var contentImage: Bitmap? = null) {
    object NotInitialized : ContentBlockState()
    object VideoLoading : ContentBlockState()
    class VideoLoaded(contentImage: Bitmap?) : ContentBlockState(contentImage)
    class VideoProcessing(contentImage: Bitmap?) : ContentBlockState(contentImage)
    class VideoProcessed(contentImage: Bitmap?) : ContentBlockState(contentImage)
}

data class ContentBlockHandler(
    val requestVideoAction: () -> Unit
)

@Preview
@Composable
fun ContentBlockDefaultPreview() {
    ContentBlock()
}

@Composable
fun ContentBlock(
    state: ContentBlockState = ContentBlockState.NotInitialized,
    contentBlockHandler: ContentBlockHandler = ContentBlockHandler({}),
) {
    Column {
        Text(
            text = stringResource(R.string.video_title),
        )

        Box(modifier = Modifier
            .height(250.dp)
            .fillMaxWidth()
            .padding(16.dp)) {

            when (state) {
                is ContentBlockState.NotInitialized -> {
                    Surface(modifier = Modifier
                        .fillMaxSize()
                        .clickable { contentBlockHandler.requestVideoAction.invoke() },
                        color = Color.LightGray,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = stringResource(R.string.request_video))
                        }
                    }
                }
                is ContentBlockState.VideoLoaded -> {
                    state.contentImage?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null
                        )
                    }
                }
                is ContentBlockState.VideoLoading -> {
                    Surface(modifier = Modifier.fillMaxSize(), color = Color.LightGray) {
                        Box() {
                            Text(text = stringResource(R.string.video_loading))
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}