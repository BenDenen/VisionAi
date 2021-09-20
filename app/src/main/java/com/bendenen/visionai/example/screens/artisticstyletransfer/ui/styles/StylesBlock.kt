package com.bendenen.visionai.example.screens.artisticstyletransfer.ui.styles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bendenen.visionai.example.R
import com.bendenen.visionai.example.ui.AppTheme
import com.bendenen.visionai.tflite.styletransfer.step.Style


data class StylesBlockState(
    val styles: List<Style> = emptyList(),
    var selectedStyle: Style? = null,
    var layoutState: LayoutState = LayoutState.DISABLED
) {

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
    AppTheme(darkTheme = true){
        StylesBlock(
            state = StylesBlockState(),
            styleImageLoader = StyleImageLoader.Impl(LocalContext.current)
        )
    }
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

private val imageSize = Modifier.size(120.dp, 120.dp)

@Composable
fun StylesBlock(
    state: StylesBlockState = StylesBlockState(),
    handler: StylesBlockHandler = StylesBlockHandler({}, {}),
    styleImageLoader: StyleImageLoader,
) {

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.styles_title),
            style = typography.subtitle1
        )

        when (state.layoutState) {
            StylesBlockState.LayoutState.DISABLED -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(top = 8.dp, bottom = 8.dp),
                    border = BorderStroke(2.dp, Color.LightGray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = stringResource(R.string.video_is_not_selected),
                            style = typography.button
                        )
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
    LazyRow(
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        items(
            count = styles.size + 1,
            itemContent = { index ->
                if (index in styles.indices) {
                    StyleCard(
                        styles[index], styleImageLoader
                    ) {
                        if (selectedStyle != it) {
                            styleClickAction(it)
                        }
                    }
                } else {
                    AddNewStyleCard(
                        addStyleAction
                    )
                }
            }
        )
    }
}

@Composable
fun CardText(text: String) {
    Text(
        text,
        modifier = Modifier
            .width(120.dp)
            .padding(top = 4.dp, bottom = 4.dp),
        maxLines = 1,
        textAlign = TextAlign.Center,
        softWrap = true
    )
}

@Composable
fun StyleCard(
    styleItem: Style,
    styleImageLoader: StyleImageLoader,
    styleClickAction: (Style) -> Unit
) {
    Card(
        modifier = Modifier.padding(start = 4.dp, end = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {

        Column(
            modifier = Modifier
                .clickable { styleClickAction.invoke(styleItem) }
        ) {
            styleImageLoader.loadStyleImage(styleItem)?.let { bitmap ->
                Box(modifier = imageSize) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } ?: Box(modifier = imageSize)
            CardText(styleItem.name)
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.LightGray,
                    elevation = 2.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_add_circle_black_24dp),
                        contentDescription = null
                    )
                }
            }
            CardText(stringResource(R.string.add_new_style))
        }

    }
}