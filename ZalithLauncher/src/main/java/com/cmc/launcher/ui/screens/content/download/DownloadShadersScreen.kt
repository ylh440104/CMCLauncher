package com.cmc.launcher.ui.screens.content.download

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import com.cmc.launcher.game.download.addon.downloadDependenciesForVersions
import com.cmc.launcher.game.download.addon.downloadSingleForVersions
import com.cmc.launcher.game.download.addon.platform.PlatformClasses
import com.cmc.launcher.ui.screens.NestedNavDisplay
import com.cmc.launcher.ui.screens.NestedNavKey
import com.cmc.launcher.ui.screens.NormalNavKey
import com.cmc.launcher.ui.screens.TitledNavKey
import com.cmc.launcher.ui.screens.content.download.addon.download.DownloadAddonScreen
import com.cmc.launcher.ui.screens.content.download.addon.elements.DownloadSingleOperation
import com.cmc.launcher.ui.screens.content.download.addon.search.SearchShadersScreen
import com.cmc.launcher.ui.screens.navigateTo
import com.cmc.launcher.ui.screens.onBack
import com.cmc.launcher.ui.screens.rememberTransitionSpec
import com.cmc.launcher.utils.network.isUsingMobileData
import com.cmc.launcher.viewmodel.ErrorViewModel
import com.cmc.launcher.viewmodel.EventViewModel

@Composable
fun DownloadShadersScreen(
    key: NestedNavKey.DownloadShaders,
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadShadersScreenKey: TitledNavKey?,
    onCurrentKeyChange: (TitledNavKey?) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    eventViewModel: EventViewModel
) {
    val backStack = key.backStack
    val stackTopKey = backStack.lastOrNull()
    LaunchedEffect(stackTopKey) {
        onCurrentKeyChange(stackTopKey)
    }

    val context = LocalContext.current

    //下载资源操作
    var operation by remember { mutableStateOf<DownloadSingleOperation>(DownloadSingleOperation.None) }
    DownloadSingleOperation(
        operation = operation,
        changeOperation = { operation = it },
        doInstall = { classes, version, versions, dependencies ->
            downloadSingleForVersions(
                version = version,
                versions = versions,
                folder = classes.versionFolder.folderName,
                submitError = submitError
            )
            downloadDependenciesForVersions(
                requests = dependencies,
                versions = versions,
                submitError = submitError
            )
        },
        onDependencyClicked = { platform, projectId, classes ->
            backStack.navigateTo(
                NormalNavKey.DownloadAddon(platform, projectId, classes)
            )
        }
    )

    if (backStack.isNotEmpty()) {
        NestedNavDisplay(
            ownerKey = key,
            backStack = backStack,
            modifier = Modifier.fillMaxSize(),
            onBack = {
                onBack(backStack)
            },
            extraDecorators = listOf(
                rememberViewModelStoreNavEntryDecorator()
            ),
            transitionSpec = rememberTransitionSpec(),
            popTransitionSpec = rememberTransitionSpec(),
            entryProvider = entryProvider {
                entry<NormalNavKey.SearchShaders> {
                    SearchShadersScreen(
                        mainScreenKey = mainScreenKey,
                        downloadScreenKey = downloadScreenKey,
                        downloadShadersScreenKey = key,
                        downloadShadersScreenCurrentKey = downloadShadersScreenKey
                    ) { platform, projectId, _ ->
                        backStack.navigateTo(
                            NormalNavKey.DownloadAddon(platform, projectId, PlatformClasses.SHADERS)
                        )
                    }
                }
                entry<NormalNavKey.DownloadAddon> { addonKey ->
                    DownloadAddonScreen(
                        mainScreenKey = mainScreenKey,
                        parentScreenKey = key,
                        parentCurrentKey = downloadScreenKey,
                        currentKey = downloadShadersScreenKey,
                        key = addonKey,
                        eventViewModel = eventViewModel,
                        onItemClicked = { classes, version, _, deps ->
                            operation = if (isUsingMobileData(context)) {
                                DownloadSingleOperation.WarningForMobileData(classes, version, deps)
                            } else {
                                DownloadSingleOperation.SelectVersion(classes, version, deps)
                            }
                        }
                    )
                }
            }
        )
    } else {
        Box(Modifier.fillMaxSize())
    }
}