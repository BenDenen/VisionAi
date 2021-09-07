package com.bendenen.visionai.example.screens.artisticstyletransfer.ui.styles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bendenen.visionai.example.R
import com.bendenen.visionai.tflite.styletransfer.step.Style


class StylesBlockState(
    styleList: List<Style> = emptyList(),
    var selectedStyle: Style? = null,
    var layoutState: LayoutState = LayoutState.DISABLED
) {

    val styles = mutableListOf<Style>().also {
        it.addAll(styleList)
    }

    enum class LayoutState {
        DISABLED,
        ENABLED,
        STYLE_PROCESSING,
        STYLE_PROCESSED,
        VIDEO_PROCESSING,
        VIDEO_PROCESSED
    }
}

data class StylesBlockHandler(
    val styleClickAction: (Style) -> Unit,
    val addStyleAction: () -> Unit
)

@Preview("With data")
@Composable
fun StyleBlockPreviewEnabled() {
    StylesBlock(
        state = StylesBlockState(
            mutableListOf<Style>().also {
                it.add(Style.AssetStyle("flowers.jpg", " Flowers"))
            },
            null,
            StylesBlockState.LayoutState.ENABLED
        ),
        styleImageLoader = StyleImageLoader.Impl(LocalContext.current)
    )
}

@Preview("Video is not selected")
@Composable
fun StyleBlockPreviewDisabled() {
    StylesBlock(
        state = StylesBlockState(),
        styleImageLoader = StyleImageLoader.Impl(LocalContext.current)
    )
}

@Preview("Style is processing")
@Composable
fun StyleBlockPreviewLoading() {
    StylesBlock(
        state = StylesBlockState(
            mutableListOf<Style>().also {
                it.add(Style.AssetStyle("flowers.jpg", " Flowers"))
            },
            null,
            StylesBlockState.LayoutState.STYLE_PROCESSING
        ),
        styleImageLoader = StyleImageLoader.Impl(LocalContext.current)
    )
}

@Composable
fun StylesBlock(
    state: StylesBlockState = StylesBlockState(),
    handler: StylesBlockHandler = StylesBlockHandler({}, {}),
    styleImageLoader: StyleImageLoader,
) {

    Column {
        Text(
            text = stringResource(R.string.styles_title),
            style = typography.subtitle1
        )

        when (state.layoutState) {
            StylesBlockState.LayoutState.DISABLED -> {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    border = BorderStroke(2.dp, Color.LightGray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text(text = stringResource(R.string.video_is_not_selected), style = typography.button)
                    }
                }
            }
            else -> {
                StyleCardBlock(
                    state.styles,
                    state.selectedStyle,
                    styleImageLoader,
                    handler.styleClickAction,
                    handler.addStyleAction
                )
            }
        }
    }
}

@Composable
fun StyleCardBlock(
    styles: List<Style>,
    selectedStyle: Style?,
    styleImageLoader: StyleImageLoader,
    styleClickAction: (Style) -> Unit,
    addStyleAction: () -> Unit
) {
    Row {
        styles.forEach { styleItem ->
            StyleCard(
                styleItem, styleImageLoader
            ) {
                if (selectedStyle != it) {
                    styleClickAction(it)
                }
            }
        }
        AddNewStyleCard(
            addStyleAction
        )
    }
}

private val imageSize = Modifier.size(100.dp, 100.dp)

@Composable
fun cardText(text: String) {
    Text(
        text,
        modifier = Modifier.wrapContentSize(),
        maxLines = 1
    )
}

@Composable
fun StyleCard(
    styleItem: Style,
    styleImageLoader: StyleImageLoader,
    styleClickAction: (Style) -> Unit
) {
    Card(shape = RoundedCornerShape(8.dp)) {

        Column(
            modifier = Modifier.clickable { styleClickAction.invoke(styleItem) }
        ) {
            styleImageLoader.loadStyleImage(styleItem)?.let { bitmap ->
                Box(modifier = imageSize) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null
                    )
                }
            } ?: Box(modifier = imageSize)
            cardText(styleItem.name)
        }
    }
}

@Composable
fun AddNewStyleCard(addStyleAction: () -> Unit) {
    Card(shape = RoundedCornerShape(8.dp)) {

        Column(
            modifier = Modifier.clickable { addStyleAction.invoke() }
        ) {
            Box(modifier = imageSize) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.LightGray, elevation = 2.dp) {
                    Image(painter = painterResource(id = R.drawable.ic_add_circle_black_24dp), contentDescription = null)
                }
            }
            cardText(stringResource(R.string.add_new_style))
        }

    }
}