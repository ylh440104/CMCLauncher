package com.cmc.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FakeShadowUp(
    modifier: Modifier = Modifier,
    height: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x3A000000),
                    )
                )
            )
    )
}

@Composable
fun FakeShadowDown(
    modifier: Modifier = Modifier,
    height: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x3A000000),
                        Color.Transparent,
                    )
                )
            )
    )
}

@Composable
fun FakeShadowLeft(
    modifier: Modifier = Modifier,
    width: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .width(width)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x1A000000),
                        Color.Transparent,
                    )
                )
            )
    )
}

@Composable
fun FakeShadowRight(
    modifier: Modifier = Modifier,
    width: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .width(width)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x1A000000),
                    )
                )
            )
    )
}

@Composable
@Preview(showBackground = true)
private fun PreviewFakeShadows() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FakeShadowUp(
            modifier = Modifier.fillMaxWidth()
        )
        FakeShadowDown(
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            FakeShadowLeft(
                modifier = Modifier.fillMaxHeight()
            )
            FakeShadowRight(
                modifier = Modifier.fillMaxHeight()
            )
        }
    }
}