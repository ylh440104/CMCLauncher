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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cmc.launcher.R
import com.cmc.launcher.game.plugin.Plugin
import com.cmc.launcher.game.plugin.driver.Driver
import com.cmc.launcher.game.plugin.driver.DriverPluginManager
import com.cmc.launcher.game.renderer.ConfigurableRenderer
import com.cmc.launcher.game.renderer.RendererInterface
import com.cmc.launcher.game.renderer.Renderers
import com.cmc.launcher.game.version.installed.GraphicsApi
import com.cmc.launcher.path.URL_CLOUD_DRIVE_DRIVER_PLUGINS
import com.cmc.launcher.path.URL_CLOUD_RENDERER_PLUGINS
import com.cmc.launcher.path.URL_GITHUB_DRIVER_PLUGINS
import com.cmc.launcher.path.URL_GITHUB_RENDERER_PLUGINS
import com.cmc.launcher.setting.AllSettings
import com.cmc.launcher.setting.enums.ResolutionRule
import com.cmc.launcher.setting.unit.floatRange
import com.cmc.launcher.ui.base.BaseScreen
import com.cmc.launcher.ui.components.AnimatedColumn
import com.cmc.launcher.ui.components.IntInputField
import com.cmc.launcher.ui.components.SimpleAlertDialog
import com.cmc.launcher.ui.components.TitleAndSummary
import com.cmc.launcher.ui.components.animated
import com.cmc.launcher.ui.components.verticalScrollWithBar
import com.cmc.launcher.ui.screens.NestedNavKey
import com.cmc.launcher.ui.screens.NormalNavKey
import com.cmc.launcher.ui.screens.TitledNavKey
import com.cmc.launcher.ui.screens.content.settings.layouts.CardPosition
import com.cmc.launcher.ui.screens.content.settings.layouts.IntSliderSettingsCard
import com.cmc.launcher.ui.screens.content.settings.layouts.ListSettingsCard
import com.cmc.launcher.ui.screens.content.settings.layouts.SettingsCard
import com.cmc.launcher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.cmc.launcher.ui.screens.content.settings.layouts.SwitchSettingsCard
import com.cmc.launcher.utils.animation.getAnimateTween
import com.cmc.launcher.utils.customResolutionRange
import com.cmc.launcher.utils.device.checkVulkanSupport
import com.cmc.launcher.utils.ensureCustomResolutionInitialized
import com.cmc.launcher.utils.getRealScreenSize
import com.cmc.launcher.utils.isAdrenoGPU
import com.cmc.launcher.viewmodel.EventViewModel
import com.cmc.launcher.viewmodel.sendDLPlugin

@Composable
fun RendererSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    eventViewModel: EventViewModel,
) {
    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.Renderer, settingsScreenKey, false)
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
                    val currentRendererId = AllSettings.renderer.state
                    val currentRenderer = remember(currentRendererId) {
                        Renderers.getRenderers()
                            .find { it.getUniqueIdentifier() == currentRendererId }
                    }
                    val configurableRenderer = remember(currentRenderer) {
                        (currentRenderer as? ConfigurableRenderer)
                            ?.takeIf { it.configSpec.entries.isNotEmpty() }
                    }
                    var showConfigDialog by remember { mutableStateOf(false) }

                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.renderer,
                        items = Renderers.getRenderers(),
                        title = "渲染后端",
                        summary = "选择图形渲染方案，影响游戏画面质量和性能",
                        getItemText = { it.getRendererName() },
                        getItemId = { it.getUniqueIdentifier() },
                        getItemSummary = {
                            RendererSummaryLayout(it)
                        },
                        trailingIcon = {
                            if (configurableRenderer != null) {
                                IconButton(
                                    onClick = { showConfigDialog = true }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_settings_filled),
                                        contentDescription = "配置"
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    eventViewModel.sendDLPlugin(
                                        githubLink = URL_GITHUB_RENDERER_PLUGINS,
                                        cloudDrives = listOf(
                                            EventViewModel.Event.DownloadPlugins.CloudDrive(
                                                language = "zh",
                                                link = URL_CLOUD_RENDERER_PLUGINS
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
                    )

                    if (showConfigDialog && configurableRenderer != null) {
                        RendererConfigDialog(
                            renderer = configurableRenderer,
                            onDismissRequest = { showConfigDialog = false }
                        )
                    }

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.sfpewPluginLayer,
                        enabled = currentRenderer is Plugin,
                        title = "插件渲染层",
                        summary = "为渲染器插件启用额外的处理层"
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.sfpewRelaxedContext,
                        enabled = currentRenderer?.let { renderer ->
                            renderer.supportsSfpewByDefault() ||
                            // 如果是插件，需要看是否给插件启用了这个玩意
                            (renderer is Plugin && AllSettings.sfpewPluginLayer.state)
                        } == true,
                        title = stringResource(R.string.renderer_config_sfpew_relaxed_context_title),
                        summary = stringResource(R.string.renderer_config_sfpew_relaxed_context_desc)
                    )

                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.vulkanDriver,
                        items = DriverPluginManager.getDriverList(),
                        title = "Vulkan 驱动",
                        getItemText = { it.name },
                        getItemId = { it.id },
                        getItemSummary = {
                            DriverSummaryLayout(it)
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    eventViewModel.sendDLPlugin(
                                        githubLink = URL_GITHUB_DRIVER_PLUGINS,
                                        cloudDrives = listOf(
                                            EventViewModel.Event.DownloadPlugins.CloudDrive(
                                                language = "zh",
                                                link = URL_CLOUD_DRIVE_DRIVER_PLUGINS
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
                    )

                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.graphicsApi,
                        items = GraphicsApi.entries,
                        title = "图形 API",
                        summary = "指定游戏使用的图形接口，默认自动选择",
                        getItemText = {
                            when (it) {
                                GraphicsApi.DEFAULT -> "自动"
                                GraphicsApi.DEFAULT_OPENGL -> "OpenGL"
                                else -> it.displayName
                            }
                        }
                    )
                }
            }

            AnimatedItem { ctx ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animated(ctx)
                ) {
                    val context = LocalContext.current

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 分辨率规则
                        val resolutionRule = AllSettings.resolutionRule.state
                        ListSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Top,
                            unit = AllSettings.resolutionRule,
                            items = ResolutionRule.entries,
                            title = "分辨率模式",
                            summary = "游戏画面的分辨率规则",
                            getItemText = { stringResource(it.nameRes) },
                            onValueChange = { rule ->
                                // 自定义分辨率尚未初始化时，以屏幕真实宽高填充
                                if (rule == ResolutionRule.CUSTOM) {
                                    ensureCustomResolutionInitialized(context)
                                }
                            }
                        )

                        // 百分比分辨率
                        AnimatedVisibility(
                            visible = resolutionRule == ResolutionRule.PERCENTAGE,
                            enter = fadeIn(animationSpec = getAnimateTween()) +
                                    expandVertically(animationSpec = getAnimateTween()),
                            exit = fadeOut(animationSpec = getAnimateTween()) +
                                    shrinkVertically(animationSpec = getAnimateTween())
                        ) {
                            IntSliderSettingsCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                position = CardPosition.Middle,
                                unit = AllSettings.resolutionRatio,
                                title = "分辨率缩放",
                                summary = "按屏幕百分比缩放游戏分辨率",
                                valueRange = AllSettings.resolutionRatio.floatRange,
                                suffix = "%",
                                fineTuningControl = true
                            )
                        }

                        // 自定义分辨率
                        AnimatedVisibility(
                            visible = resolutionRule == ResolutionRule.CUSTOM,
                            enter = fadeIn(animationSpec = getAnimateTween()) +
                                    expandVertically(animationSpec = getAnimateTween()),
                            exit = fadeOut(animationSpec = getAnimateTween()) +
                                    shrinkVertically(animationSpec = getAnimateTween())
                        ) {
                            CustomResolutionSettingsCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                position = CardPosition.Middle
                            )
                        }
                    }

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.gameFullScreen,
                        title = "全屏模式",
                        summary = "以全屏方式运行游戏"
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
                        unit = AllSettings.sustainedPerformance,
                        title = "持续性能模式",
                        summary = "限制峰值性能以维持长时间稳定帧率"
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.requestMaxRefreshRate,
                        title = "最高刷新率",
                        summary = "请求屏幕以最高刷新率运行"
                    )

                    if (checkVulkanSupport(LocalContext.current.packageManager)) {
                        var adrenoGPUAlert by remember { mutableStateOf(false) }

                        SwitchSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            unit = AllSettings.zinkPreferSystemDriver,
                            title = "优先系统 Vulkan 驱动",
                            summary = "使用系统自带的 Vulkan 驱动而非 Turnip",
                            onCheckedChange = { checked ->
                                if (checked && isAdrenoGPU()) adrenoGPUAlert = true
                            }
                        )

                        if (adrenoGPUAlert) {
                            SimpleAlertDialog(
                                title = stringResource(R.string.generic_warning),
                                text = "Adreno GPU 使用系统 Vulkan 驱动可能出现花屏，确认要开启吗？",
                                onConfirm = {
                                    AllSettings.zinkPreferSystemDriver.save(true)
                                    adrenoGPUAlert = false
                                },
                                onDismiss = {
                                    AllSettings.zinkPreferSystemDriver.save(false)
                                    adrenoGPUAlert = false
                                }
                            )
                        }
                    }

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.vsyncInZink,
                        title = "垂直同步",
                        summary = "开启 VSync 可减少画面撕裂但可能限制帧率"
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.dumpShaders,
                        title = "着色器转储",
                        summary = "导出编译后的着色器，用于问题排查"
                    )
                }
            }
        }
    }
}

@Composable
fun RendererSummaryLayout(renderer: RendererInterface) {
    FlowRow(
        modifier = Modifier.alpha(0.7f),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        with(renderer) {
            getRendererSummary()?.let { summary ->
                Text(text = summary, style = MaterialTheme.typography.labelSmall)
            }

            val minVer = getDisplayMinMCVersion()
            val maxVer = getDisplayMaxMCVersion()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (minVer != null || maxVer != null) {
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
}

@Composable
fun DriverSummaryLayout(driver: Driver) {
    with(driver) {
        summary?.let { text ->
            Text(
                modifier = Modifier.alpha(0.7f),
                text = text, style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun CustomResolutionSettingsCard(
    modifier: Modifier = Modifier,
    position: CardPosition = CardPosition.Middle
) {
    val context = LocalContext.current
    val screenSize = remember(context) { getRealScreenSize(context) }

    SettingsCard(
        modifier = modifier,
        position = position
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TitleAndSummary(
                title = "分辨率缩放",
                summary = "手动指定游戏的渲染分辨率"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IntInputField(
                    modifier = Modifier.weight(1f),
                    value = AllSettings.customResolutionWidth.state,
                    permitted = customResolutionRange(screenSize.width),
                    label = "宽度",
                    onValueChange = { AllSettings.customResolutionWidth.save(it) }
                )
                IntInputField(
                    modifier = Modifier.weight(1f),
                    value = AllSettings.customResolutionHeight.state,
                    permitted = customResolutionRange(screenSize.height),
                    label = "高度",
                    onValueChange = { AllSettings.customResolutionHeight.save(it) }
                )
            }
        }
    }
}