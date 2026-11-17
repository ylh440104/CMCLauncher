package com.cmc.launcher.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cmc.launcher.ui.theme.itemColor
import com.cmc.launcher.ui.theme.onItemColor

@Composable
fun RadioCard(
    selected: Boolean,
    text: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
    radioColors: RadioButtonColors = RadioButtonDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    RadioCard(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        color = color,
        contentColor = contentColor,
        radioColors = radioColors,
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            style = textStyle
        )
    }
}

@Composable
fun RadioCard(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
    shape: Shape = MaterialTheme.shapes.large,
    radioColors: RadioButtonColors = RadioButtonDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    InternalSurface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = shape,
        onClick = onClick,
        interactionSource = interactionSource,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier.padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                colors = radioColors,
                interactionSource = interactionSource
            )

            content()
        }
    }
}

@Composable
private fun InternalSurface(
    color: Color,
    contentColor: Color,
    shape: Shape,
    onClick: (() -> Unit)?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Surface(
            modifier = modifier,
            color = color,
            contentColor = contentColor,
            shape = shape,
            onClick = onClick,
            enabled = enabled,
            interactionSource = interactionSource,
            content = content
        )
    } else {
        Surface(
            modifier = modifier,
            color = color,
            contentColor = contentColor,
            shape = shape,
            content = content
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRadioCard() {
    MaterialExpressiveTheme {
        Column(
            modifier = Modifier.padding(all = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioCard(
                selected = true,
                text = "Selected",
                onClick = {}
            )
            RadioCard(
                selected = false,
                text = "Unselected",
                onClick = {}
            )
        }
    }
}