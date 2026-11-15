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

package com.cmc.launcher.ui.screens.content

import androidx.compose.foundation.basicMarquee
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import com.cmc.launcher.R
import com.cmc.launcher.game.download.addon.favorites.FavoriteProjectsRepository
import com.cmc.launcher.game.download.addon.platform.Platform
import com.cmc.launcher.game.download.addon.platform.PlatformClasses
import com.cmc.launcher.ui.base.BaseScreen
import com.cmc.launcher.ui.components.AnimatedDirection
import com.cmc.launcher.ui.components.animated
import com.cmc.launcher.ui.components.fadeEdge
import com.cmc.launcher.ui.components.rememberAnimatedContext
import com.cmc.launcher.ui.screens.NestedNavDisplay
import com.cmc.launcher.ui.screens.NestedNavKey
import com.cmc.launcher.ui.screens.NormalNavKey
import com.cmc.launcher.ui.screens.TitledNavKey
import com.cmc.launcher.ui.screens.content.download.DownloadFavoritesScreen
import com.cmc.launcher.ui.screens.content.download.DownloadGameScreen
import com.cmc.launcher.ui.screens.content.download.DownloadModPackScreen
import com.cmc.launcher.ui.screens.content.download.DownloadModScreen
import com.cmc.launcher.ui.screens.content.download.DownloadResourcePackScreen
import com.cmc.launcher.ui.screens.content.download.DownloadSavesScreen
import com.cmc.launcher.ui.screens.content.download.DownloadShadersScreen
import com.cmc.launcher.ui.screens.content.download.addon.search.SearchIdScreen
import com.cmc.launcher.ui.screens.content.elements.CategoryIcon
import com.cmc.launcher.ui.screens.content.elements.CategoryItem
import com.cmc.launcher.ui.screens.navigateOnce
import com.cmc.launcher.ui.screens.onBack
import com.cmc.launcher.ui.screens.rememberTransitionSpec
import com.cmc.launcher.viewmodel.ErrorViewModel
import com.cmc.launcher.viewmodel.EventViewModel
import com.cmc.launcher.viewmodel.ModpackImportViewModel
import com.cmc.launcher.viewmodel.ScreenBackStackViewModel

/**
 * 导航至DownloadScreen
 */
fun ScreenBackStackViewModel.navigateToDownload(targetScreen: TitledNavKey? = null) {
    downloadScreen.clearWith(targetScreen ?: downloadGameScreen)
    mainScreen.removeAndNavigateTo(
        removes = clearBeforeNavKeys,
        screenKey = downloadScreen,
        useClassEquality = true
    )
}

/**
 * 跳转到资源类型对应的下载分类屏幕，并进入项目详情页
 */
private fun ScreenBackStackViewModel.swapToCategoryAddon(
    platform: Platform,
    classes: PlatformClasses,
    projectId: String,
    iconUrl: String?
) {
    val targetScreen = when (classes) {
        PlatformClasses.MOD -> downloadModScreen
        PlatformClasses.MOD_PACK -> downloadModPackScreen
        PlatformClasses.RESOURCE_PACK -> downloadResourcePackScreen
        PlatformClasses.SAVES -> downloadSavesScreen
        PlatformClasses.SHADERS -> downloadShadersScreen
    }
    navigateToDownload(
        targetScreen = targetScreen.apply {
            navigateTo(
                NormalNavKey.DownloadAddon(
                    platform = platform,
                    projectId = projectId,
                    classes = classes,
                    iconUrl = iconUrl
                )
            )
        }
    )
}

@Composable
fun DownloadScreen(
    key: NestedNavKey.Download,
    backScreenViewModel: ScreenBackStackViewModel,
    modpackImportViewModel: ModpackImportViewModel,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    //进入下载屏幕时确保收藏仓库完成初始化
    LaunchedEffect(Unit) {
        FavoriteProjectsRepository.ensureLoaded()
    }

    var showHub by remember { mutableStateOf(true) }

    BackHandler(enabled = !showHub) {
        showHub = true
    }

    BaseScreen(
        screenKey = key,
        currentKey = backScreenViewModel.mainScreen.currentKey,
        useClassEquality = true
    ) { isVisible: Boolean ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (!showHub) {
                NavigationUI(
                    key = key,
                    backScreenViewModel = backScreenViewModel,
                    eventViewModel = eventViewModel,
                    modpackImportViewModel = modpackImportViewModel,
                    submitError = submitError,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                DownloadHub(
                    isVisible = isVisible,
                    backScreenViewModel = backScreenViewModel,
                    onOpenCategory = { target ->
                        showHub = false
                        key.backStack.navigateOnce(target)
                    }
                )
            }
        }
    }
}

private data class HubEntry(
    val target: TitledNavKey,
    val title: String,
    val caption: String,
    @DrawableRes val icon: Int,
    val container: Color,
    val content: Color
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DownloadHub(
    isVisible: Boolean,
    backScreenViewModel: ScreenBackStackViewModel,
    onOpenCategory: (TitledNavKey) -> Unit
) {
    val ctx = rememberAnimatedContext(isVisible = isVisible)
    val scheme = MaterialTheme.colorScheme

    val entries = listOf(
        HubEntry(
            target = backScreenViewModel.downloadGameScreen,
            title = "游戏本体",
            caption = "原版与加载器安装",
            icon = R.drawable.ic_sports_esports_outlined,
            container = scheme.primaryContainer,
            content = scheme.onPrimaryContainer
        ),
        HubEntry(
            target = backScreenViewModel.downloadModPackScreen,
            title = "整合包",
            caption = "一键装配完整环境",
            icon = R.drawable.ic_package_2_outlined,
            container = scheme.secondaryContainer,
            content = scheme.onSecondaryContainer
        ),
        HubEntry(
            target = backScreenViewModel.downloadModScreen,
            title = "模组",
            caption = "扩展玩法内容",
            icon = R.drawable.ic_extension_outlined,
            container = scheme.tertiaryContainer,
            content = scheme.onTertiaryContainer
        ),
        HubEntry(
            target = backScreenViewModel.downloadResourcePackScreen,
            title = "资源包",
            caption = "替换材质与音效",
            icon = R.drawable.ic_format_paint_outlined,
            container = scheme.primaryContainer,
            content = scheme.onPrimaryContainer
        ),
        HubEntry(
            target = backScreenViewModel.downloadSavesScreen,
            title = "存档",
            caption = "下载他人世界",
            icon = R.drawable.ic_public,
            container = scheme.secondaryContainer,
            content = scheme.onSecondaryContainer
        ),
        HubEntry(
            target = backScreenViewModel.downloadShadersScreen,
            title = "光影包",
            caption = "提升画面表现",
            icon = R.drawable.ic_lightbulb,
            container = scheme.tertiaryContainer,
            content = scheme.onTertiaryContainer
        ),
        HubEntry(
            target = NormalNavKey.SearchId,
            title = "按 ID 查找",
            caption = "已知项目编号直达",
            icon = R.drawable.ic_card,
            container = scheme.surfaceContainerHigh,
            content = scheme.onSurfaceVariant
        ),
        HubEntry(
            target = backScreenViewModel.downloadFavoritesScreen,
            title = "我的收藏",
            caption = "稍后再看的内容",
            icon = R.drawable.ic_favorite_outlined,
            container = scheme.surfaceContainerHigh,
            content = scheme.onSurfaceVariant
        )
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .animated(ctx, from = AnimatedDirection.Top),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 6.dp)
            ) {
                Text(
                    text = "下载中心",
                    style = MaterialTheme.typography.headlineMedium,
                    color = scheme.onBackground
                )
                Text(
                    text = "内容来自 Modrinth",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }

        items(entries) { entry ->
            HubCard(entry = entry, onClick = { onOpenCategory(entry.target) })
        }
    }
}

@Composable
private fun HubCard(
    entry: HubEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = entry.container),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(entry.content.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(23.dp),
                    painter = painterResource(entry.icon),
                    contentDescription = null,
                    tint = entry.content
                )
            }

            Column {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = entry.content,
                    maxLines = 1
                )
                Text(
                    text = entry.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = entry.content.copy(alpha = 0.75f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun NavigationUI(
    key: NestedNavKey.Download,
    backScreenViewModel: ScreenBackStackViewModel,
    eventViewModel: EventViewModel,
    modpackImportViewModel: ModpackImportViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = key.backStack
    val stackTopKey = backStack.lastOrNull()
    LaunchedEffect(stackTopKey) {
        backScreenViewModel.downloadScreen.currentKey = stackTopKey
    }

    if (backStack.isNotEmpty()) {
        NestedNavDisplay(
            ownerKey = key,
            backStack = backStack,
            modifier = modifier,
            onBack = {
                onBack(backStack)
            },
            transitionSpec = rememberTransitionSpec(),
            popTransitionSpec = rememberTransitionSpec(),
            entryProvider = entryProvider {
                entry<NestedNavKey.DownloadGame> { key ->
                    DownloadGameScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadGameScreenKey = backScreenViewModel.downloadGameScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadGameScreen.currentKey = newKey
                        },
                        eventViewModel = eventViewModel
                    )
                }
                entry<NestedNavKey.DownloadModPack> { key ->
                    DownloadModPackScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadModPackScreenKey = backScreenViewModel.downloadModPackScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadModPackScreen.currentKey = newKey
                        },
                        eventViewModel = eventViewModel,
                        importerViewModel = modpackImportViewModel
                    )
                }
                entry<NestedNavKey.DownloadMod> { key ->
                    DownloadModScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadModScreenKey = backScreenViewModel.downloadModScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadModScreen.currentKey = newKey
                        },
                        submitError = submitError,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NestedNavKey.DownloadResourcePack> { key ->
                    DownloadResourcePackScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadResourcePackScreenKey = backScreenViewModel.downloadResourcePackScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadResourcePackScreen.currentKey = newKey
                        },
                        submitError = submitError,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NestedNavKey.DownloadSaves> { key ->
                    DownloadSavesScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadSavesScreenKey = backScreenViewModel.downloadSavesScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadSavesScreen.currentKey = newKey
                        },
                        submitError = submitError,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NestedNavKey.DownloadShaders> { key ->
                    DownloadShadersScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadShadersScreenKey = backScreenViewModel.downloadShadersScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadShadersScreen.currentKey = newKey
                        },
                        submitError = submitError,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NormalNavKey.SearchId> {
                    SearchIdScreen(
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        swapToDownload = { platform, classes, projectId, iconUrl ->
                            backScreenViewModel.swapToCategoryAddon(platform, classes, projectId, iconUrl)
                        },
                        openLink = { link ->
                            eventViewModel.sendEvent(EventViewModel.Event.OpenLink(link))
                        }
                    )
                }
                entry<NestedNavKey.DownloadFavorites> { key ->
                    DownloadFavoritesScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadFavoritesScreenKey = backScreenViewModel.downloadFavoritesScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadFavoritesScreen.currentKey = newKey
                        },
                        swapToDownload = { platform, classes, projectId, iconUrl ->
                            backScreenViewModel.swapToCategoryAddon(platform, classes, projectId, iconUrl)
                        }
                    )
                }
            }
        )
    } else {
        Box(modifier)
    }
}