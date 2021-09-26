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
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bendenen.visionai.example.R

interface MainScreenLayoutHandler {
    fun onRequestStyleTransfer() {}
    fun onRequestSegmentation() {}
    fun onRequestArCore() {}
}

@Preview
@Composable
fun DefaultPreview() {
    MainScreenLayout(object : MainScreenLayoutHandler {})
}

@Composable
fun MainScreenLayout(
    handler: MainScreenLayoutHandler
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Tile(
            R.drawable.starry_night,
            R.string.request_style_transfer
        ) { handler.onRequestSegmentation() }
        Tile(
            R.drawable.arcore_dev,
            R.string.request_ar_core
        ) { handler.onRequestArCore() }
    }

}

@Composable
fun Tile(
    @DrawableRes imageId: Int,
    @StringRes titleId: Int,
    clickAction: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
        modifier = Modifier.clickable { clickAction.invoke() }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = BiasAlignment.Horizontal(0f)
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painterResource(id = imageId),
                    contentDescription = null,
                    modifier = Modifier.size(200.dp, 200.dp)
                )
            }
            Text(
                stringResource(titleId),
                modifier = Modifier.padding(top = 8.dp),
                style = typography.body1
            )
        }
    }
}