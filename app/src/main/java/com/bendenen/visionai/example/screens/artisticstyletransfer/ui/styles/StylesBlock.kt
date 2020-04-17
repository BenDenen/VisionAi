package com.bendenen.visionai.example.screens.artisticstyletransfer.ui.styles

import androidx.compose.Composable
import androidx.compose.Model
import androidx.compose.frames.ModelList
import androidx.ui.core.Alignment
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.clip
import androidx.ui.foundation.Border
import androidx.ui.foundation.Box
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.HorizontalScroller
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.ScaleFit
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.LayoutHeight
import androidx.ui.layout.LayoutPadding
import androidx.ui.layout.LayoutSize
import androidx.ui.layout.LayoutWidth
import androidx.ui.layout.Row
import androidx.ui.layout.preferredSize
import androidx.ui.material.Card
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.material.Typography
import androidx.ui.material.ripple.ripple
import androidx.ui.res.stringResource
import androidx.ui.res.vectorResource
import androidx.ui.text.style.TextOverflow
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.bendenen.visionai.example.R
import com.bendenen.visionai.example.ui.BitmapImageAsset
import com.bendenen.visionai.example.ui.themeTypography
import com.bendenen.visionai.tflite.styletransfer.step.Style

@Model
class StylesBlockState(
    styleList: List<Style> = emptyList(),
    var selectedStyle: Style? = null,
    var layoutState: LayoutState = LayoutState.DISABLED
) {

    val styles: ModelList<Style> = ModelList<Style>().also {
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
            ModelList<Style>().also {
                it.add(Style.AssetStyle("flowers.jpg", " Flowers"))
            },
            null,
            StylesBlockState.LayoutState.ENABLED
        ),
        styleImageLoader = StyleImageLoader.Impl(ContextAmbient.current)
    )
}

@Preview("Video is not selected")
@Composable
fun StyleBlockPreviewDisabled() {
    StylesBlock(
        state = StylesBlockState(),
        styleImageLoader = StyleImageLoader.Impl(ContextAmbient.current)
    )
}

@Preview("Style is processing")
@Composable
fun StyleBlockPreviewLoading() {
    StylesBlock(
        state = StylesBlockState(
            ModelList<Style>().also {
                it.add(Style.AssetStyle("flowers.jpg", " Flowers"))
            },
            null,
            StylesBlockState.LayoutState.STYLE_PROCESSING
        ),
        styleImageLoader = StyleImageLoader.Impl(ContextAmbient.current)
    )
}

@Composable
fun StylesBlock(
    state: StylesBlockState = StylesBlockState(),
    handler: StylesBlockHandler = StylesBlockHandler({}, {}),
    styleImageLoader: StyleImageLoader,
    typography: Typography = themeTypography
) {

    Column {
        Text(
            text = stringResource(R.string.styles_title),
            style = typography.subtitle1,
            modifier = LayoutPadding(start = 8.dp, top = 8.dp)
        )

        when (state.layoutState) {
            StylesBlockState.LayoutState.DISABLED -> {
                Surface(
                    modifier = LayoutWidth.Fill + LayoutHeight(188.dp) + LayoutPadding(32.dp),
                    border = Border(2.dp, Color.LightGray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(gravity = Alignment.Center) {
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
    addStyleAction: () -> Unit,
    typography: Typography = themeTypography
) {
    HorizontalScroller {
        Row(modifier = LayoutPadding(16.dp)) {
            AddNewStyleCard(
                typography,
                addStyleAction
            )
            styles.forEach { styleItem ->
                StyleCard(
                    styleItem, styleImageLoader, typography = typography
                ) {
                    if (selectedStyle != it) {
                        styleClickAction(it)
                    }
                }
            }
        }
    }
}

private val imageSize = Modifier.preferredSize(100.dp, 100.dp)

@Composable
fun cardText(text: String, typography: Typography) {
    Text(
        text,
        style = typography.overline,
        modifier = LayoutPadding(8.dp) + LayoutWidth.Max(84.dp),
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )
}

@Composable
fun StyleCard(
    styleItem: Style,
    styleImageLoader: StyleImageLoader,
    typography: Typography,
    color: Color = MaterialTheme.colors.surface,
    border: Border? = null,
    styleClickAction: (Style) -> Unit
) {
    Card(shape = RoundedCornerShape(8.dp), color = color, border = border, modifier = LayoutPadding(start = 16.dp)) {
        Clickable(
            modifier = Modifier.ripple(true),
            onClick = {
                styleClickAction(styleItem)
            }
        ) {
            Column(
                arrangement = Arrangement.Center
            ) {
                styleImageLoader.loadStyleImage(styleItem)?.let { bitmap ->
                    val imageModifier = Modifier.clip(shape = RoundedCornerShape(8.dp))
                    Image(
                        modifier = imageModifier + imageSize,
                        scaleFit = ScaleFit.FillMaxDimension,
                        asset = BitmapImageAsset(bitmap)
                    )
                } ?: Box(modifier = imageSize)
                cardText(styleItem.name, typography)
            }
        }
    }
}

@Composable
fun AddNewStyleCard(typography: Typography, addStyleAction: () -> Unit) {
    Card(shape = RoundedCornerShape(8.dp)) {
        Clickable(
            modifier = Modifier.ripple(true),
            onClick = {
                addStyleAction()
            }) {
            Column(
                arrangement = Arrangement.Center
            ) {
                Box(modifier = imageSize, shape = RoundedCornerShape(topLeft = 8.dp, topRight = 8.dp)) {
                    Surface(modifier = LayoutSize.Fill, color = Color.LightGray, elevation = 2.dp) {
                        Icon(vectorResource(R.drawable.ic_add_circle_black_24dp))
                    }
                }
                cardText(stringResource(R.string.add_new_style), typography)
            }

        }
    }
}