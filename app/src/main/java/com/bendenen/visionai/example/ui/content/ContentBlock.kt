package com.bendenen.visionai.example.ui.content

import android.graphics.Bitmap
import androidx.compose.Composable
import androidx.compose.Model
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.graphics.Color
import androidx.ui.layout.Column
import androidx.ui.layout.Container
import androidx.ui.layout.LayoutPadding
import androidx.ui.layout.LayoutSize
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.padding
import androidx.ui.layout.preferredHeight
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.Surface
import androidx.ui.material.Typography
import androidx.ui.material.ripple.ripple
import androidx.ui.res.stringResource
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.bendenen.visionai.example.R
import com.bendenen.visionai.example.ui.BitmapImageAsset
import com.bendenen.visionai.example.ui.themeTypography

@Model
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
    contentBlockHandler: ContentBlockHandler = ContentBlockHandler(
        {}),
    typography: Typography = themeTypography
) {
    Column {
        Text(
            text = stringResource(R.string.video_title),
            style = typography.subtitle1,
            modifier = LayoutPadding(start = 8.dp, top = 8.dp)
        )

        Box(modifier = Modifier.preferredHeight(250.dp) + Modifier.fillMaxWidth() + Modifier.padding(16.dp)) {

            when (state) {
                is ContentBlockState.NotInitialized -> {
                    Clickable(
                        modifier = Modifier.ripple(),
                        onClick = {
                            contentBlockHandler.requestVideoAction()
                        }
                    ) {
                        Surface(modifier = LayoutSize.Fill, color = Color.LightGray) {
                            Container(alignment = Alignment.Center) {
                                Text(text = stringResource(R.string.request_video), style = typography.button)
                            }
                        }
                    }
                }
                is ContentBlockState.VideoLoaded -> {
                    state.contentImage?.let {
                        Image(asset = BitmapImageAsset(it))
                    }
                }
                is ContentBlockState.VideoLoading -> {
                    Surface(modifier = LayoutSize.Fill, color = Color.LightGray) {
                        Container(alignment = Alignment.Center) {
                            Text(text = stringResource(R.string.video_loading), style = typography.button)
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}