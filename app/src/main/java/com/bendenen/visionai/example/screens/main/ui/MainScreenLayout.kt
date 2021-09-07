package com.bendenen.visionai.example.screens.main.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bendenen.visionai.example.R

@Preview
@Composable
fun DefaultPreview() {
    MainScreenLayout({}, {})
}

@Composable
fun MainScreenLayout(
    requestStyleTransferAction: () -> Unit,
    requestSegmentationAction: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Tile(R.drawable.starry_night, R.string.request_style_transfer, requestStyleTransferAction)
        }

    }
}

@Composable
fun Tile(
    @DrawableRes imageId: Int,
    @StringRes titleId: Int,
    clickAction: () -> Unit
) {
    Card(shape = RoundedCornerShape(8.dp), elevation = 4.dp) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .clickable { clickAction.invoke() },
            horizontalAlignment = BiasAlignment.Horizontal(0f)
        ) {

            Box(modifier = Modifier
                .size(200.dp, 200.dp)
                .clip(RoundedCornerShape(8.dp))) {
                Image(painter = painterResource(id = imageId), contentDescription = null)
            }
            Text(
                stringResource(titleId),
                modifier = Modifier.padding(top = 8.dp),
                style = typography.body1
            )
        }
    }
}