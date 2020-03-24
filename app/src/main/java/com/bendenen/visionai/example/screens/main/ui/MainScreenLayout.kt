package com.bendenen.visionai.example.screens.main.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.Composable
import androidx.ui.core.Clip
import androidx.ui.core.Text
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.SimpleImage
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.Container
import androidx.ui.layout.LayoutPadding
import androidx.ui.layout.LayoutSize
import androidx.ui.layout.LayoutWidth
import androidx.ui.layout.Row
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Typography
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Card
import androidx.ui.res.imageResource
import androidx.ui.res.stringResource
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.bendenen.visionai.example.R
import com.bendenen.visionai.example.ui.themeTypography

@Preview
@Composable
fun DefaultPreview() {
    MainScreenLayout({}, {})
}

@Composable
fun MainScreenLayout(
    requestStyleTransferAction: () -> Unit,
    requestSegmentationAction: () -> Unit,
    typography: Typography = themeTypography
) {
    MaterialTheme {
        Column(
            arrangement = Arrangement.Center,
            modifier = LayoutSize.Fill
        ) {
            Row(arrangement = Arrangement.Center, modifier = LayoutWidth.Fill) {
                Tile(typography, R.drawable.starry_night, R.string.request_style_transfer, requestStyleTransferAction)
            }

            // TODO: 2020-04-09 Update Remove background with Compose UI
//            Row(arrangement = Arrangement.Center, modifier = LayoutWidth.Fill + LayoutPadding(top = 16.dp)) {
//                Tile(typography, R.drawable.segmentation, R.string.request_body_segmentation, requestSegmentationAction)
//            }
        }
    }
}

@Composable
fun Tile(
    typography: Typography,
    @DrawableRes imageId: Int,
    @StringRes titleId: Int,
    clickAction: () -> Unit
) {
    Card(shape = RoundedCornerShape(8.dp), elevation = 4.dp) {
        Ripple(bounded = true) {
            Clickable(onClick = {
                clickAction()
            }) {
                Column(
                    modifier = LayoutPadding(16.dp),
                    arrangement = Arrangement.Center
                ) {

                    Container(modifier = LayoutSize(200.dp, 200.dp)) {
                        Clip(shape = RoundedCornerShape(8.dp)) {
                            SimpleImage(imageResource(imageId))
                        }
                    }
                    Text(
                        stringResource(titleId),
                        modifier = LayoutPadding(top = 8.dp),
                        style = typography.body1
                    )
                }
            }

        }
    }
}