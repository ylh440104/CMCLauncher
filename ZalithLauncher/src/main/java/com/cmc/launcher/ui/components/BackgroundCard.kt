package com.cmc.launcher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.cmc.launcher.setting.AllSettings
import com.cmc.launcher.ui.screens.content.elements.backgroundGlass
import com.cmc.launcher.ui.glass.LocalGlassHost
import com.cmc.launcher.ui.glass.glassPanel
import com.cmc.launcher.ui.screens.content.elements.liquidGlassHighlight
import com.cmc.launcher.ui.theme.cmc.Cmc
import com.cmc.launcher.ui.theme.cardColor
import com.cmc.launcher.ui.theme.cardTitleColor
import com.cmc.launcher.ui.theme.onCardColor

@Composable
private fun flatCardElevation() = CardDefaults.cardElevation(
    defaultElevation = 0.dp,
    pressedElevation = 0.dp,
    focusedElevation = 0.dp,
    hoveredElevation = 0.dp,
    draggedElevation = 0.dp,
    disabledElevation = 0.dp
)

/**
 * 背景卡片组件，
 * 使用方式与原本的[Card]无异，但[BackgroundCard]配置了更舒适的背景颜色
 */
@Composable
fun BackgroundCard(
    modifier: Modifier = Modifier,
    influencedByBackground: Boolean = true,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = cardColor(influencedByBackground),
        contentColor = onCardColor()
    ),
    elevation: CardElevation = flatCardElevation(),
    blur: Int = AllSettings.backgroundBlur.state,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border ?: cmcCardBorder(),
    ) {
        val glass = LocalGlassHost.current
        Column(
            modifier = Modifier
                .then(
                    if (glass != null && influencedByBackground) {
                        Modifier.glassPanel(glass)
                    } else {
                        Modifier.backgroundGlass(blur, colors.containerColor, influencedByBackground)
                    }
                )
                .liquidGlassHighlight(),
            content = content
        )
    }
}

private fun cmcCardBorder(): BorderStroke = BorderStroke(
    width = 1.dp,
    brush = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = 0.45f),
            Color.White.copy(alpha = 0.12f),
            Color.Transparent
        )
    )
)

/**
 * 背景卡片组件，
 * 使用方式与原本的[Card]无异，但[BackgroundCard]配置了更舒适的背景颜色
 */
@Composable
fun BackgroundCard(
    modifier: Modifier = Modifier,
    influencedByBackground: Boolean = true,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = cardColor(influencedByBackground),
        contentColor = onCardColor(),
        disabledContainerColor = cardColor(influencedByBackground)
    ),
    elevation: CardElevation = flatCardElevation(),
    blur: Int = AllSettings.backgroundBlur.state,
    border: BorderStroke? = null,
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable @UiComposable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border ?: cmcCardBorder(),
        onClick = onClick,
        enabled = enabled,
    ) {
        Column(
            modifier = Modifier
                .backgroundGlass(blur, colors.containerColor, influencedByBackground)
                .liquidGlassHighlight(),
            content = content
        )
    }
}
/**
 * 适合在背景卡片组件顶部使用的标题栏Layout
 * @param alpha 组件背景颜色不透明度
 */
@Composable
fun CardTitleLayout(
    modifier: Modifier = Modifier,
    influencedByBackground: Boolean = true,
    alpha: Float = 0.5f,
    color: Color = influencedByBackgroundColor(
        color = cardTitleColor(alpha),
        enabled = influencedByBackground
    ),
    contentColor: Color = onCardColor(),
    blur: Int = AllSettings.backgroundBlur.state,
    content: @Composable @UiComposable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = color,
            contentColor = contentColor,
        ) {
            Column(
                modifier = Modifier.backgroundGlass(blur, color, influencedByBackground),
                content = content
            )
        }
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
    }
}