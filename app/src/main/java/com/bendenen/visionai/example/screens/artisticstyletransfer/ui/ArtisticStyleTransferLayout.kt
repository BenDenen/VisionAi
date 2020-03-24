package com.bendenen.visionai.example.screens.artisticstyletransfer.ui

import android.graphics.Bitmap
import androidx.compose.Composable
import androidx.compose.Model
import androidx.ui.core.Alignment
import androidx.ui.core.ContextAmbient
import androidx.ui.foundation.DrawBackground
import androidx.ui.graphics.Color
import androidx.ui.graphics.vector.DrawVector
import androidx.ui.layout.Column
import androidx.ui.layout.Container
import androidx.ui.layout.LayoutPadding
import androidx.ui.layout.LayoutSize
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.FloatingActionButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Typography
import androidx.ui.res.vectorResource
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.bendenen.visionai.example.R
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.content.ContentBlock
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.content.ContentBlockHandler
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.content.ContentBlockState
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.styles.StyleImageLoader
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.styles.StylesBlock
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.styles.StylesBlockHandler
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.styles.StylesBlockState
import com.bendenen.visionai.example.ui.themeTypography
import com.bendenen.visionai.tflite.styletransfer.step.Style

@Model
class ArtisticStyleTransferLayoutState {
    val stylesBlockState: StylesBlockState = StylesBlockState()
    var contentBlockState: ContentBlockState = ContentBlockState.NotInitialized
    var layoutState: LayoutState = LayoutState.NOT_INITIALIZED

    fun updateToVideoLoadingState() {
        stylesBlockState.layoutState = StylesBlockState.LayoutState.DISABLED
        contentBlockState = ContentBlockState.VideoLoading
        layoutState = LayoutState.VIDEO_LOADING
    }

    fun updateToVideoLoadedState(contentImage: Bitmap, styleList: List<Style>) {
        stylesBlockState.styles.clear()
        stylesBlockState.styles.addAll(styleList)
        stylesBlockState.layoutState = StylesBlockState.LayoutState.ENABLED
        contentBlockState = ContentBlockState.VideoLoaded(contentImage)
        layoutState = LayoutState.VIDEO_LOADED
    }

    fun updateToStyleProcessingState() {
        stylesBlockState.layoutState = StylesBlockState.LayoutState.STYLE_PROCESSING
        contentBlockState = ContentBlockState.VideoLoaded(contentBlockState.contentImage)
        layoutState = LayoutState.STYLE_PROCESSING
    }

    fun updateToStyleProcessedState(contentImage: Bitmap, style: Style) {
        stylesBlockState.layoutState = StylesBlockState.LayoutState.STYLE_PROCESSED
        contentBlockState = ContentBlockState.VideoLoaded(contentImage)
        layoutState = LayoutState.STYLE_PROCESSED
    }

    enum class LayoutState {
        NOT_INITIALIZED,
        VIDEO_LOADING,
        VIDEO_LOADED,
        STYLE_PROCESSING,
        STYLE_PROCESSED,
        VIDEO_PROCESSING,
        VIDEO_PROCESSED;

        fun isLoadingStats() = this in listOf(VIDEO_LOADING, STYLE_PROCESSING, VIDEO_PROCESSING)
    }
}

data class ArtisticStyleTransferLayoutHandler(
    val stylesBlockHandler: StylesBlockHandler = StylesBlockHandler({}, {}),
    val contentBlockHandler: ContentBlockHandler = ContentBlockHandler {},
    val processVideoAction: () -> Unit = {}
)

@Preview
@Composable
fun DefaultPreview() {
    ArtisticStyleTransferLayout(
        ArtisticStyleTransferLayoutState(),
        ArtisticStyleTransferLayoutHandler(),
        StyleImageLoader.Impl(context = ContextAmbient.current)
    )
}

@Preview
@Composable
fun LoadingPreview() {
    ArtisticStyleTransferLayout(
        ArtisticStyleTransferLayoutState().also { it.layoutState = ArtisticStyleTransferLayoutState.LayoutState.VIDEO_LOADING },
        ArtisticStyleTransferLayoutHandler(),
        StyleImageLoader.Impl(context = ContextAmbient.current)
    )
}

@Composable
fun ArtisticStyleTransferLayout(
    state: ArtisticStyleTransferLayoutState,
    handler: ArtisticStyleTransferLayoutHandler,
    styleImageLoader: StyleImageLoader,
    typography: Typography = themeTypography
) {
    MaterialTheme() {
        Column {
            StylesBlock(
                state.stylesBlockState,
                handler.stylesBlockHandler,
                styleImageLoader,
                typography
            )
            ContentBlock(
                state.contentBlockState,
                handler.contentBlockHandler,
                typography
            )
            if (state.layoutState != ArtisticStyleTransferLayoutState.LayoutState.NOT_INITIALIZED) {
                Container(modifier = LayoutSize.Fill, alignment = Alignment.BottomEnd) {
                    FloatingActionButton(modifier = LayoutPadding(16.dp), onClick = handler.processVideoAction) {
                        DrawVector(
                            vectorResource(R.drawable.ic_movie_filter_white_24dp),
                            tintColor = Color.White
                        )
                    }
                }
            }
        }
        if (state.layoutState.isLoadingStats()) {
            Container(
                modifier = LayoutSize.Fill + DrawBackground(color = Color(125, 125, 125, 100)),
                alignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
