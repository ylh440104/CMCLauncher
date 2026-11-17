package com.cmc.launcher.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cmc.launcher.R
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DefaultSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(),
    interactionSource: MutableInteractionSource? = null
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        thumbContent = {
            val rotation by animateFloatAsState(
                if (checked) 0.0f else -(180.0f),
                label = "switch_thumb_rotation"
            )
            Crossfade(
                modifier = Modifier.rotate(rotation),
                targetState = checked
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(
                        if (it) R.drawable.ic_check else R.drawable.ic_close
                    ),
                    contentDescription = null,
                    tint = colors.trackColor(enabled, checked)
                )
            }
        },
        interactionSource = interactionSource
    )
}

@Stable
private fun SwitchColors.trackColor(enabled: Boolean, checked: Boolean): Color =
    if (enabled) {
        if (checked) checkedTrackColor else uncheckedTrackColor
    } else {
        if (checked) disabledCheckedTrackColor else disabledUncheckedTrackColor
    }
