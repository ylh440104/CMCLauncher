package com.cmc.launcher.ui.screens.content.versions.layouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.cmc.launcher.ui.components.BackgroundCard

@Composable
fun VersionChunkBackground(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    shape: Shape = MaterialTheme.shapes.extraLarge,
    content: @Composable ColumnScope.() -> Unit
) {
    BackgroundCard(
        modifier = modifier.fillMaxWidth(),
        shape = shape
    ) {
        Column(
            modifier = Modifier.padding(paddingValues),
            content = content
        )
    }
}