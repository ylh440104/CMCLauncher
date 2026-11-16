package com.cmc.launcher.ui.screens.content.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cmc.launcher.R
import com.cmc.launcher.setting.AllSettings
import com.cmc.launcher.setting.enums.MirrorSourceType
import com.cmc.launcher.ui.base.BaseScreen
import com.cmc.launcher.ui.components.animated
import com.cmc.launcher.ui.components.rememberAnimatedContext
import com.cmc.launcher.ui.screens.NestedNavKey
import com.cmc.launcher.ui.screens.NormalNavKey
import com.cmc.launcher.ui.screens.TitledNavKey

private data class MirrorOption(
    val type: MirrorSourceType,
    val title: String,
    val caption: String,
    val icon: Int
)

private val mirrorOptions = listOf(
    MirrorOption(
        type = MirrorSourceType.AUTO,
        title = "自动选择",
        caption = "按网络环境自动挑选最快的线路",
        icon = R.drawable.ic_autorenew
    ),
    MirrorOption(
        type = MirrorSourceType.MIRROR,
        title = "镜像优先",
        caption = "BMCLAPI 加速，国内下载更快",
        icon = R.drawable.ic_rocket_launch_filled
    ),
    MirrorOption(
        type = MirrorSourceType.OFFICIAL,
        title = "官方源",
        caption = "直连 Mojang 官方服务器",
        icon = R.drawable.ic_public
    )
)

@Composable
fun MirrorSettingScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?
) {
    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.Mirror, settingsScreenKey, false)
    ) { isVisible ->
        val ctx = rememberAnimatedContext(isVisible = isVisible)
        val listState = rememberLazyListState()
        val current = AllSettings.gameDownloadSource.state

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .animated(ctx),
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 6.dp)) {
                    Text(
                        text = "下载镜像",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "选择游戏与依赖的下载线路",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(mirrorOptions) { option ->
                MirrorCard(
                    option = option,
                    selected = current == option.type,
                    onSelect = { AllSettings.gameDownloadSource.save(option.type) }
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "说明",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            modifier = Modifier.padding(top = 4.dp),
                            text = "镜像优先会先尝试国内加速节点，失败时自动回落到官方源；官方源适合海外网络环境。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 2.dp)) {
                    Text(
                        text = "外观",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "毛玻璃与液态玻璃质感",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(22.dp),
                            painter = painterResource(R.drawable.ic_water_drop_filled),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text(
                                text = "液态玻璃",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "高光、折射与顶部亮边",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = AllSettings.liquidGlass.state,
                            onCheckedChange = { AllSettings.liquidGlass.save(it) }
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                modifier = Modifier.size(22.dp),
                                painter = painterResource(R.drawable.ic_blur_circular_filled),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp)
                            ) {
                                Text(
                                    text = "模糊强度",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "玻璃背后的虚化程度",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${AllSettings.backgroundBlur.state}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            value = AllSettings.backgroundBlur.state.toFloat(),
                            onValueChange = { AllSettings.backgroundBlur.updateState(it.toInt()) },
                            onValueChangeFinished = {
                                AllSettings.backgroundBlur.save(AllSettings.backgroundBlur.state)
                            },
                            valueRange = 0f..40f,
                            steps = 39
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MirrorCard(
    option: MirrorOption,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val container = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val content = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = container),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                modifier = Modifier.size(22.dp),
                painter = painterResource(option.icon),
                contentDescription = null,
                tint = content
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = content
                )
                Text(
                    text = option.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = content.copy(alpha = 0.75f)
                )
            }
            RadioButton(
                selected = selected,
                onClick = onSelect
            )
        }
    }
}