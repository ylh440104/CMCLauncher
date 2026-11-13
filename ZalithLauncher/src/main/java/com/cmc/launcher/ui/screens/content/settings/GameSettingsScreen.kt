/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui.screens.content.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cmc.launcher.R
import com.cmc.launcher.game.multirt.RuntimesManager
import com.cmc.launcher.game.plugin.natives.NativePlugin
import com.cmc.launcher.game.plugin.natives.NativePluginManager
import com.cmc.launcher.path.URL_CLOUD_NATIVE_LIB_PLUGINS
import com.cmc.launcher.path.URL_GITHUB_NATIVE_LIB_PLUGINS
import com.cmc.launcher.setting.AllSettings
import com.cmc.launcher.setting.unit.floatRange
import com.cmc.launcher.setting.unit.min
import com.cmc.launcher.ui.base.BaseScreen
import com.cmc.launcher.ui.components.AnimatedColumn
import com.cmc.launcher.ui.components.animated
import com.cmc.launcher.ui.components.verticalScrollWithBar
import com.cmc.launcher.ui.screens.NestedNavKey
import com.cmc.launcher.ui.screens.NormalNavKey
import com.cmc.launcher.ui.screens.TitledNavKey
import com.cmc.launcher.ui.screens.content.elements.MemoryPreview
import com.cmc.launcher.ui.screens.content.settings.layouts.CardPosition
import com.cmc.launcher.ui.screens.content.settings.layouts.IntSliderSettingsCard
import com.cmc.launcher.ui.screens.content.settings.layouts.ListSettingsCard
import com.cmc.launcher.ui.screens.content.settings.layouts.SettingsCard
import com.cmc.launcher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.cmc.launcher.ui.screens.content.settings.layouts.StringListSettingsCard
import com.cmc.launcher.ui.screens.content.settings.layouts.SwitchSettingsCard
import com.cmc.launcher.ui.screens.content.settings.layouts.TextInputSettingsCard
import com.cmc.launcher.utils.platform.getMaxMemoryForSettings
import com.cmc.launcher.viewmodel.EventViewModel
import com.cmc.launcher.viewmodel.sendDLPlugin

@Composable
fun GameSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    eventViewModel: EventViewModel
) {
    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.Game, settingsScreenKey, false)
    ) { isVisible ->
        AnimatedColumn(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScrollWithBar(state = rememberScrollState())
                .padding(all = 12.dp),
            isVisible = isVisible
        ) {
            AnimatedItem { ctx ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animated(ctx)
                ) {
                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.versionIsolation,
                        title = "版本隔离",
                        summary = "每个版本使用独立的游戏目录，存档和模组互不干扰"
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.skipGameIntegrityCheck,
                        title = "跳过完整性校验",
                        summary = "启动时不校验游戏文件哈希，加快启动速度"
                    )

                    TextInputSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.versionCustomInfo,
                        title = "自定义版本信息",
                        summary = "替换主菜单右下角显示的版本号文字"
                    )
                }
            }

            AnimatedItem { ctx ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animated(ctx)
                ) {
                    val runtimes = remember { RuntimesManager.getRuntimes() }

                    if (runtimes.isNotEmpty()) {
                        ListSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Top,
                            unit = AllSettings.javaRuntime,
                            items = RuntimesManager.getRuntimes().filter { it.isCompatible() },
                            title = "Java 运行时",
                            summary = "选择游戏使用的 Java 版本",
                            getItemText = { it.name },
                            getItemId = { it.name }
                        )
                    }

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = if (runtimes.isNotEmpty()) CardPosition.Middle else CardPosition.Top,
                        unit = AllSettings.autoPickJavaRuntime,
                        title = "自动选择 Java",
                        summary = "根据版本要求自动匹配合适的 Java 运行时"
                    )

                    val nativePlugins = remember {
                        NativePluginManager.getPlugins()
                    }

                    @Composable
                    fun DLNativeLibsButton() {
                        IconButton(
                            onClick = {
                                eventViewModel.sendDLPlugin(
                                    githubLink = URL_GITHUB_NATIVE_LIB_PLUGINS,
                                    cloudDrives = listOf(
                                        EventViewModel.Event.DownloadPlugins.CloudDrive(
                                            language = "zh",
                                            link = URL_CLOUD_NATIVE_LIB_PLUGINS
                                        )
                                    )
                                )
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_download_2_filled),
                                contentDescription = stringResource(R.string.generic_download)
                            )
                        }
                    }
                    if (nativePlugins.isNotEmpty()) {
                        StringListSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            unit = AllSettings.disableNativeLibPlugins,
                            items = nativePlugins,
                            onItemsChange = { value, item ->
                                if (value) {
                                    this - item.packageName
                                } else {
                                    this + item.packageName
                                }
                            },
                            title = "原生库插件",
                            summary = "管理已安装的原生库插件，影响模组兼容性",
                            getItemID = { it.packageName },
                            getItemText = { it.displayName },
                            getItemSummary = { plugin ->
                                NativePluginSummaryLayout(plugin)
                            },
                            getItemCheck = { contains -> !contains },
                            trailingIcon = {
                                DLNativeLibsButton()
                            },
                        )
                    } else {
                        SettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            title = "原生库插件",
                            summary = "管理已安装的原生库插件，影响模组兼容性",
                            onClick = {},
                            trailingIcon = {
                                DLNativeLibsButton()
                            }
                        )
                    }

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.ramAllocation,
                        title = "内存分配",
                        summary = "为游戏分配的内存大小，过大可能导致系统卡顿",
                        valueRange = AllSettings.ramAllocation.floatRange.start..getMaxMemoryForSettings(LocalContext.current).toFloat(),
                        suffix = "MB",
                        fineTuningControl = true,
                        previewContent = {
                            MemoryPreview(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 2.dp),
                                preview = (AllSettings.ramAllocation.state ?: AllSettings.ramAllocation.min).toDouble(),
                                usedText = { usedMemory, totalMemory ->
                                    "已用 " + usedMemory.toInt() + " / " + totalMemory.toInt() + " MB"
                                },
                                previewText = { preview ->
                                    "分配 " + preview.toInt() + " MB"
                                }
                            )
                        }
                    )

                    TextInputSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.jvmArgs,
                        title = "JVM 参数",
                        summary = "额外的 Java 虚拟机启动参数，留空使用默认值"
                    )
                }
            }

            AnimatedItem { ctx ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animated(ctx)
                ) {
                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.showLogAutomatic,
                        title = "自动显示日志",
                        summary = "游戏启动后自动打开日志窗口"
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.logTextSize,
                        title = "日志字号",
                        summary = "调整游戏日志的文字大小",
                        valueRange = AllSettings.logTextSize.floatRange,
                        suffix = "Sp",
                        fineTuningControl = true
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.logBufferFlushInterval,
                        title = "日志刷新间隔",
                        summary = "日志缓冲区的写入间隔，越小越实时但越耗电",
                        valueRange = AllSettings.logBufferFlushInterval.floatRange,
                        suffix = "ms",
                        fineTuningControl = true
                    )
                }
            }
        }
    }
}


@Composable
private fun NativePluginSummaryLayout(plugin: NativePlugin) {
    FlowRow(
        modifier = Modifier.alpha(0.7f),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_renderer_from_plugins,plugin.appName),
            style = MaterialTheme.typography.labelSmall
        )

        val minVer = plugin.minMCVer
        val maxVer = plugin.maxMCVer

        if (minVer != null || maxVer != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = stringResource(R.string.renderer_version_support), style = MaterialTheme.typography.labelSmall)

                minVer?.let {
                    Text(text = ">= $it", style = MaterialTheme.typography.labelSmall)
                }

                maxVer?.let {
                    Text(text = "<= $it", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}