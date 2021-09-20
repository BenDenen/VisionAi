package com.bendenen.visionai.example.screens.artisticstyletransfer.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bendenen.visionai.example.R
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.content.ContentBlock
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.content.ContentBlockHandler
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.content.ContentBlockState
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.styles.StyleImageLoader
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.styles.StylesBlock
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.styles.StylesBlockHandler
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.styles.StylesBlockState
import com.bendenen.visionai.tflite.styletransfer.step.Style

data class ArtisticStyleTransferLayoutState(
    val stylesBlockState: StylesBlockState = StylesBlockState(),
    var contentBlockState: ContentBlockState = ContentBlockState.NotInitialized,
    var layoutState: LayoutState = LayoutState.NOT_INITIALIZED
) {


    fun toVideoLoadingState() = this.copy(
        stylesBlockState = stylesBlockState.copy(layoutState = StylesBlockState.LayoutState.DISABLED),
        contentBlockState = ContentBlockState.VideoLoading,
        layoutState = LayoutState.VIDEO_LOADING
    )

    fun toVideoLoadedState(contentImage: Bitmap, styleList: List<Style>) = this.copy(
        stylesBlockState = stylesBlockState.copy(
            styles = styleList,
            layoutState = StylesBlockState.LayoutState.ENABLED
        ),
        contentBlockState = ContentBlockState.VideoLoaded(contentImage),
        layoutState = LayoutState.VIDEO_LOADED
    )

    fun toStyleProcessingState() = this.copy(
        stylesBlockState = stylesBlockState.copy(
            layoutState = StylesBlockState.LayoutState.STYLE_PROCESSING
        ),
        contentBlockState = ContentBlockState.VideoLoaded(contentBlockState.contentImage),
        layoutState = LayoutState.STYLE_PROCESSING
    )

    fun toStyleProcessedState(contentImage: Bitmap, style: Style) = this.copy(
        stylesBlockState = stylesBlockState.copy(
            layoutState = StylesBlockState.LayoutState.STYLE_PROCESSED
        ),
        contentBlockState = ContentBlockState.VideoLoaded(contentImage),
        layoutState = LayoutState.STYLE_PROCESSED
    )

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
        MutableLiveData(ArtisticStyleTransferLayoutState()),
        ArtisticStyleTransferLayoutHandler(),
        StyleImageLoader.Impl(context = LocalContext.current)
    )
}

@Preview
@Composable
fun LoadingPreview() {
    ArtisticStyleTransferLayout(
        MutableLiveData(ArtisticStyleTransferLayoutState(layoutState = ArtisticStyleTransferLayoutState.LayoutState.VIDEO_LOADING)),
        ArtisticStyleTransferLayoutHandler(),
        StyleImageLoader.Impl(context = LocalContext.current)
    )
}

@Composable
fun ArtisticStyleTransferLayout(
    liveDataState: LiveData<ArtisticStyleTransferLayoutState>,
    handler: ArtisticStyleTransferLayoutHandler,
    styleImageLoader: StyleImageLoader,
) {
    val state by liveDataState.observeAsState(
        liveDataState.value ?: ArtisticStyleTransferLayoutState()
    )
    MaterialTheme() {
        Column {
            StylesBlock(
                state.stylesBlockState,
                handler.stylesBlockHandler,
                styleImageLoader,
            )
            ContentBlock(
                state.contentBlockState,
                handler.contentBlockHandler,
            )
            if (state.layoutState != ArtisticStyleTransferLayoutState.LayoutState.NOT_INITIALIZED) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
                    FloatingActionButton(
                        modifier = Modifier.padding(16.dp),
                        onClick = handler.processVideoAction
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_movie_filter_white_24dp),
                            contentDescription = null
                        )
                    }
                }
            }
        }
        if (state.layoutState.isLoadingStats()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.wrapContentSize())
            }
        }
    }
}
