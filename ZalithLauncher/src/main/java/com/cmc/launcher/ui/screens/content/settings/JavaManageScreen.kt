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

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.cmc.launcher.ui.components.CmcTag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cmc.launcher.R
import com.cmc.launcher.ZLApplication
import com.cmc.launcher.context.getFileName
import com.cmc.launcher.coroutine.Task
import com.cmc.launcher.coroutine.TaskSystem
import com.cmc.launcher.game.launch.executeJarWithUri
import com.cmc.launcher.game.multirt.Runtime
import com.cmc.launcher.game.multirt.RuntimesManager
import com.cmc.launcher.path.PathManager
import com.cmc.launcher.setting.AllSettings
import com.cmc.launcher.ui.AndroidStringText
import com.cmc.launcher.ui.androidText
import com.cmc.launcher.ui.base.BaseScreen
import com.cmc.launcher.ui.components.CardTitleLayout
import com.cmc.launcher.ui.components.IconTextButton
import com.cmc.launcher.ui.components.MarqueeText
import com.cmc.launcher.ui.components.SimpleAlertDialog
import com.cmc.launcher.ui.components.animated
import com.cmc.launcher.ui.components.rememberAnimatedContext
import com.cmc.launcher.ui.components.rememberDialogMaxHeight
import com.cmc.launcher.ui.screens.NestedNavKey
import com.cmc.launcher.ui.screens.NormalNavKey
import com.cmc.launcher.ui.screens.TitledNavKey
import com.cmc.launcher.ui.screens.content.elements.ImportMultipleFileButton
import com.cmc.launcher.ui.screens.content.elements.ImportSingleFileButton
import com.cmc.launcher.ui.screens.content.settings.layouts.CardPosition
import com.cmc.launcher.ui.screens.content.settings.layouts.SettingsCard
import com.cmc.launcher.ui.theme.cardColor
import com.cmc.launcher.ui.theme.itemColor
import com.cmc.launcher.ui.theme.onCardColor
import com.cmc.launcher.ui.theme.onItemColor
import com.cmc.launcher.utils.animation.getAnimateTween
import com.cmc.launcher.utils.device.Architecture
import com.cmc.launcher.utils.file.checkExtensionOrThrow
import com.cmc.launcher.utils.string.getMessageOrToString
import com.cmc.launcher.utils.string.throwableToString
import com.cmc.launcher.viewmodel.ErrorViewModel
import com.cmc.launcher.viewmodel.EventViewModel
import com.cmc.launcher.viewmodel.sendToast
import kotlinx.coroutines.Dispatchers

private sealed interface RuntimeOperation {
    data object None: RuntimeOperation
    data class PreDelete(val runtime: Runtime): RuntimeOperation
    data class Delete(val runtime: Runtime): RuntimeOperation
}

@Composable
fun JavaManageScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current

    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.JavaManager, settingsScreenKey, false)
    ) { isVisible ->
        val ctx = rememberAnimatedContext(isVisible = isVisible)

        var runtimes by remember { mutableStateOf(getRuntimes()) }
        var runtimeOperation by remember { mutableStateOf<RuntimeOperation>(RuntimeOperation.None) }

        var showSelectRuntimeDialog by remember { mutableStateOf(false) }
        var selectedRuntimeForJar by remember { mutableStateOf<Runtime?>(null) }
        var launchJarPicker by remember { mutableStateOf<(() -> Unit)?>(null) }

        RuntimeOperation(
            runtimeOperation = runtimeOperation,
            updateOperation = { runtimeOperation = it },
            callRefresh = { runtimes = getRuntimes(true) },
            submitError = submitError
        )

        if (showSelectRuntimeDialog) {
            SelectJavaRuntimeDialog(
                runtimes = runtimes,
                onDismissRequest = {
                    showSelectRuntimeDialog = false
                    launchJarPicker = null
                },
                onSelectRuntime = { selectedRuntime ->
                    showSelectRuntimeDialog = false
                    selectedRuntimeForJar = selectedRuntime
                    launchJarPicker?.invoke()
                    launchJarPicker = null
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .animated(ctx)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { runtimes = getRuntimes(true) }
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(R.drawable.ic_refresh),
                        contentDescription = null
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "刷新",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                ImportMultipleFileButton(
                    extension = "xz",
                    progressUris = { uris ->
                        uris.forEach { uri ->
                            progressRuntimeUri(
                                context = context,
                                uri = uri,
                                callRefresh = { runtimes = getRuntimes(true) },
                                submitError = submitError
                            )
                        }
                    }
                )
                ImportSingleFileButton(
                    extension = "jar",
                    onClick = {
                        selectedRuntimeForJar = null
                    },
                    onLongClick = { launch ->
                        launchJarPicker = launch
                        showSelectRuntimeDialog = true
                    },
                    progressUris = { uris ->
                        uris.firstOrNull()?.let { uri ->
                            val customRuntime = selectedRuntimeForJar
                            selectedRuntimeForJar = null

                            if (customRuntime != null) {
                                (context as? Activity)?.let { activity ->
                                    executeJarWithUri(activity, uri, customRuntime.name)
                                }
                            } else {
                                RuntimesManager.getExactJreName(8) ?: run {
                                    eventViewModel.sendToast(androidText(R.string.multirt_no_java_8), Toast.LENGTH_LONG)
                                    return@ImportSingleFileButton
                                }
                                (context as? Activity)?.let { activity ->
                                    val jreName = AllSettings.javaRuntime.takeIf { AllSettings.autoPickJavaRuntime.getValue() }?.getValue()
                                    executeJarWithUri(activity, uri, jreName)
                                }
                            }
                        }
                    },
                    painter = painterResource(R.drawable.ic_terminal_outlined),
                    text = "运行 JAR"
                )
            }

            val scrollState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = scrollState,
            ) {
                items(runtimes) { runtime ->
                    JavaRuntimeItem(
                        runtime = runtime,
                        modifier = Modifier.fillMaxWidth(),
                        onDeleteClick = {
                            runtimeOperation = RuntimeOperation.PreDelete(runtime)
                        }
                    )
                }
            }
        }
    }
}

private fun getRuntimes(forceLoad: Boolean = false): List<Runtime> =
    RuntimesManager.getRuntimes(forceLoad = forceLoad)

@Composable
private fun RuntimeOperation(
    runtimeOperation: RuntimeOperation,
    updateOperation: (RuntimeOperation) -> Unit,
    callRefresh: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    when(runtimeOperation) {
        is RuntimeOperation.None -> {}
        is RuntimeOperation.PreDelete -> {
            val runtime = runtimeOperation.runtime
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.multirt_runtime_delete_message, runtime.name),
                onConfirm = { updateOperation(RuntimeOperation.Delete(runtime)) },
                onDismiss = { updateOperation(RuntimeOperation.None) }
            )
        }
        is RuntimeOperation.Delete -> {
            val failedMessage = stringResource(R.string.multirt_runtime_delete_failed)
            val runtime = runtimeOperation.runtime
            TaskSystem.submitTask(
                Task.runTask(
                    id = runtime.name,
                    dispatcher = Dispatchers.IO,
                    task = { task ->
                        task.updateMessage(androidText(
                            R.string.multirt_runtime_deleting, runtime.name
                        ))
                        RuntimesManager.removeRuntime(runtime.name)
                    },
                    onError = {
                        submitError(
                            ErrorViewModel.ThrowableMessage(
                                title = androidText(failedMessage),
                                message = androidText(it.getMessageOrToString())
                            )
                        )
                    },
                    onFinally = callRefresh
                )
            )
            updateOperation(RuntimeOperation.None)
        }
    }
}

private fun progressRuntimeUri(
    context: Context,
    uri: Uri,
    callRefresh: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    fun showError(
        title: AndroidStringText = androidText(R.string.multirt_runtime_import_failed),
        message: AndroidStringText
    ) {
        submitError(
            ErrorViewModel.ThrowableMessage(
                title = title,
                message = message
            )
        )
    }

    val name = context.getFileName(uri) ?: run {
        showError(message = androidText(R.string.multirt_runtime_import_failed_file_name))
        return
    }
    TaskSystem.submitTask(
        Task.runTask(
            id = name,
            dispatcher = Dispatchers.IO,
            task = { task ->
                name.checkExtensionOrThrow(listOf("xz"))

                val inputStream = context.contentResolver.openInputStream(uri) ?: run {
                    showError(message = androidText(R.string.multirt_runtime_import_failed_input_stream))
                    return@runTask
                }
                RuntimesManager.installRuntime(
                    nativeLibDir = PathManager.DIR_NATIVE_LIB,
                    inputStream = inputStream,
                    name = name,
                    updateProgress = { textRes, textArg ->
                        task.updateMessage(androidText(
                            textRes, *textArg
                        ))
                    }
                )
            },
            onError = {
                showError(message = androidText(throwableToString(it)))
            },
            onFinally = callRefresh,
            onCancel = {
                runCatching {
                    RuntimesManager.removeRuntime(name)
                    callRefresh()
                }.onFailure { t ->
                    showError(
                        title = androidText(R.string.multirt_runtime_delete_failed),
                        message = androidText(t.getMessageOrToString())
                    )
                }
            }
        )
    )
}

@Composable
private fun JavaRuntimeItem(
    runtime: Runtime,
    modifier: Modifier = Modifier,
    color: Color = itemColor(),
    contentColor: Color = onItemColor(),
    onClick: () -> Unit = {},
    onDeleteClick: (() -> Unit)? = null
) {
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }
    ElevatedCard(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        shape = MaterialTheme.shapes.extraLarge,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = runtime.name,
                    style = MaterialTheme.typography.titleMedium
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (runtime.isProvidedByLauncher) {
                        CmcTag(
                            text = "内置",
                            container = MaterialTheme.colorScheme.primaryContainer,
                            content = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    runtime.versionString?.let { version ->
                        CmcTag(
                            text = version,
                            container = MaterialTheme.colorScheme.secondaryContainer,
                            content = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } ?: CmcTag(
                        text = "已损坏",
                        container = MaterialTheme.colorScheme.errorContainer,
                        content = MaterialTheme.colorScheme.onErrorContainer
                    )
                    runtime.javaVersion.takeIf { it != 0 }?.let { javaVersion ->
                        CmcTag(
                            text = "Java $javaVersion",
                            container = MaterialTheme.colorScheme.surfaceContainerHighest,
                            content = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    runtime.arch?.let { arch ->
                        val compatible = ZLApplication.DEVICE_ARCHITECTURE == Architecture.archAsInt(arch)
                        CmcTag(
                            text = if (compatible) arch else "$arch 不兼容",
                            container = if (compatible) MaterialTheme.colorScheme.surfaceContainerHighest
                            else MaterialTheme.colorScheme.errorContainer,
                            content = if (compatible) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            if (onDeleteClick != null) {
                IconButton(
                    enabled = !runtime.isProvidedByLauncher || !runtime.isCompatible(),
                    onClick = onDeleteClick
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete_filled),
                        contentDescription = stringResource(R.string.generic_delete)
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectJavaRuntimeDialog(
    runtimes: List<Runtime>,
    onDismissRequest: () -> Unit,
    onSelectRuntime: (Runtime) -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .padding(all = 16.dp)
                .heightIn(max = rememberDialogMaxHeight())
                .fillMaxHeight()
                .fillMaxWidth(0.55f),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 6.dp)
                    .fillMaxWidth()
                    .heightIn(max = (maxHeight - 12.dp).coerceAtMost(rememberDialogMaxHeight()))
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                color = cardColor(false),
                contentColor = onCardColor(),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.execute_jar_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    val scrollState = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .nonInteractiveScrollbar(
                                state = scrollState.scrollIndicatorState!!,
                                orientation = Orientation.Vertical,
                            ),
                        state = scrollState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(runtimes) { runtime ->
                            JavaRuntimeItem(
                                runtime = runtime,
                                onClick = { onSelectRuntime(runtime) },
                                onDeleteClick = null
                            )
                        }
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onDismissRequest
                    ) {
                        MarqueeText(text = stringResource(R.string.generic_cancel))
                    }
                }
            }
        }
    }
}
