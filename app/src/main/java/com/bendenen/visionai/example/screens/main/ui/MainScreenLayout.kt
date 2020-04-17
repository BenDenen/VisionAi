package com.bendenen.visionai.example.screens.main.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.core.clip
import androidx.ui.foundation.Box
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.LayoutPadding
import androidx.ui.layout.LayoutSize
import androidx.ui.layout.LayoutWidth
import androidx.ui.layout.Row
import androidx.ui.layout.padding
import androidx.ui.layout.preferredSize
import androidx.ui.material.Card
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Typography
import androidx.ui.material.ripple.ripple
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
        Clickable(
            modifier = Modifier.ripple(true),
            onClick = {
                clickAction()
            }) {
            Column(
                modifier = Modifier.padding(16.dp),
                arrangement = Arrangement.Center
            ) {

                Box(modifier = Modifier.preferredSize(200.dp, 200.dp)) {
                    val imageModifier = Modifier.clip(shape = RoundedCornerShape(8.dp))
                    Image(asset = imageResource(imageId), modifier = imageModifier)
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