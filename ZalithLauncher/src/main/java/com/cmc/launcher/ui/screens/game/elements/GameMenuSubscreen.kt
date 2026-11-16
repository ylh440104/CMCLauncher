package com.cmc.launcher.ui.screens.game.elements

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cmc.launcher.R
import com.cmc.launcher.setting.AllSettings
import com.cmc.launcher.setting.enums.FpsDisplayMode
import com.cmc.launcher.setting.enums.ResolutionRule
import com.cmc.launcher.setting.unit.floatRange
import com.cmc.launcher.ui.AndroidStringText
import com.cmc.launcher.ui.androidText
import com.cmc.launcher.ui.components.IntInputField
import com.cmc.launcher.ui.components.MenuListLayout
import com.cmc.launcher.ui.components.MenuSliderLayout
import com.cmc.launcher.ui.components.MenuState
import com.cmc.launcher.ui.components.MenuSwitchButton
import com.cmc.launcher.ui.components.MenuTextButton
import com.cmc.launcher.ui.components.lazyScrollWithBar
import com.cmc.launcher.utils.customResolutionRange
import com.cmc.launcher.utils.getRealScreenSize
import com.cmc.launcher.utils.ensureCustomResolutionInitialized

@Composable
fun GameMenuSubscreen(
    state: MenuState,
    closeScreen: () -> Unit,
    onForceClose: () -> Unit,
    onSwitchLog: () -> Unit,
    enableTerracotta: Boolean,
    onOpenTerracottaMenu: () -> Unit,
    onRefreshWindowSize: () -> Unit,
    onInputMethod: () -> Unit,
    onSendKeycode: () -> Unit,
    onReplacementControl: () -> Unit,
    onEditLayout: () -> Unit,
    onShowToast: (AndroidStringText, Int) -> Unit
) {
    val visible = state == MenuState.SHOW

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = closeScreen
                    )
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInHorizontally { -60 },
            exit = fadeOut() + slideOutHorizontally { -60 }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .fillMaxHeight()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 3.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "游戏菜单",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "基础设置",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = closeScreen) {
                            Icon(
                                modifier = Modifier.size(22.dp),
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = "关闭",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    GameMenuContent(
                        modifier = Modifier.weight(1f),
                        onForceClose = onForceClose,
                        onSwitchLog = onSwitchLog,
                        enableTerracotta = enableTerracotta,
                        onOpenTerracottaMenu = onOpenTerracottaMenu,
                        onRefreshWindowSize = onRefreshWindowSize,
                        onInputMethod = onInputMethod,
                        onSendKeycode = onSendKeycode,
                        onReplacementControl = onReplacementControl,
                        onEditLayout = onEditLayout,
                        closeScreen = closeScreen,
                        onShowToast = onShowToast
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, caption: String? = null) {
    Column(modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        if (caption != null) {
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GameMenuContent(
    onForceClose: () -> Unit,
    onSwitchLog: () -> Unit,
    enableTerracotta: Boolean,
    onOpenTerracottaMenu: () -> Unit,
    onRefreshWindowSize: () -> Unit,
    onInputMethod: () -> Unit,
    onSendKeycode: () -> Unit,
    onReplacementControl: () -> Unit,
    onEditLayout: () -> Unit,
    closeScreen: () -> Unit,
    onShowToast: (AndroidStringText, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val container = MaterialTheme.colorScheme.surfaceContainerHigh
    val content = MaterialTheme.colorScheme.onSurface

    LazyColumn(
        modifier = modifier.lazyScrollWithBar(listState),
        state = listState,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item { SectionTitle("输入", "键盘与键值") }
        item {
            MenuTextButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.game_menu_option_input_method),
                onClick = {
                    onInputMethod()
                    closeScreen()
                },
                color = container,
                contentColor = content
            )
        }
        item {
            MenuTextButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.game_menu_option_send_keycode),
                onClick = {
                    onSendKeycode()
                    closeScreen()
                },
                color = container,
                contentColor = content
            )
        }

        item { SectionTitle("控制布局") }
        item {
            MenuTextButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.game_menu_option_replacement_control),
                onClick = {
                    onReplacementControl()
                    closeScreen()
                },
                color = container,
                contentColor = content
            )
        }
        item {
            MenuTextButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.control_manage_info_edit),
                onClick = {
                    onEditLayout()
                    closeScreen()
                },
                color = container,
                contentColor = content
            )
        }
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.game_menu_option_controls_opacity),
                value = AllSettings.controlsOpacity.state,
                valueRange = AllSettings.controlsOpacity.floatRange,
                onValueChange = { AllSettings.controlsOpacity.updateState(it) },
                onValueChangeFinished = { AllSettings.controlsOpacity.save(it) },
                suffix = "%",
                color = container,
                contentColor = content
            )
        }

        item { SectionTitle("显示", "悬浮球与状态信息") }
        item {
            MenuSwitchButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.game_menu_option_show_menu),
                switch = AllSettings.showMenuBall.state,
                onSwitch = { value ->
                    AllSettings.showMenuBall.save(value)
                    if (!value) {
                        onShowToast(
                            androidText(R.string.game_menu_option_show_menu_hided),
                            Toast.LENGTH_LONG
                        )
                    }
                },
                color = container,
                contentColor = content
            )
        }
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.game_menu_option_menu_ball_opacity),
                value = AllSettings.menuBallOpacity.state,
                valueRange = AllSettings.menuBallOpacity.floatRange,
                onValueChange = { value ->
                    AllSettings.menuBallOpacity.updateState(value)
                },
                onValueChangeFinished = { value ->
                    AllSettings.menuBallOpacity.save(value)
                },
                suffix = "%",
                color = container,
                contentColor = content,
                enabled = AllSettings.showMenuBall.state
            )
        }
        item {
            MenuSwitchButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.game_menu_option_switch_fps),
                switch = AllSettings.showFPS.state,
                onSwitch = { AllSettings.showFPS.save(it) },
                color = container,
                contentColor = content,
                enabled = AllSettings.showMenuBall.state
            )
        }
        item {
            MenuListLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.game_menu_option_fps_display_mode),
                items = FpsDisplayMode.entries,
                currentItem = AllSettings.fpsDisplayMode.state,
                onItemChange = { mode -> AllSettings.fpsDisplayMode.save(mode) },
                getItemText = { stringResource(it.nameRes) },
                color = container,
                contentColor = content,
                enabled = AllSettings.showMenuBall.state && AllSettings.showFPS.state
            )
        }
        item {
            MenuSwitchButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.game_menu_option_switch_memory),
                switch = AllSettings.showMemory.state,
                onSwitch = { AllSettings.showMemory.save(it) },
                color = container,
                contentColor = content,
                enabled = AllSettings.showMenuBall.state
            )
        }

        item { SectionTitle("画面", "渲染分辨率") }
        item {
            MenuListLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.settings_renderer_resolution_rule_title),
                items = ResolutionRule.entries,
                currentItem = AllSettings.resolutionRule.state,
                onItemChange = { rule ->
                    AllSettings.resolutionRule.save(rule)
                    if (rule == ResolutionRule.CUSTOM) {
                        ensureCustomResolutionInitialized(context)
                    }
                    onRefreshWindowSize()
                },
                getItemText = { stringResource(it.nameRes) },
                color = container,
                contentColor = content
            )
        }
        item {
            AnimatedVisibility(
                visible = AllSettings.resolutionRule.state == ResolutionRule.PERCENTAGE
            ) {
                MenuSliderLayout(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.settings_renderer_resolution_scale_title),
                    value = AllSettings.resolutionRatio.state,
                    valueRange = AllSettings.resolutionRatio.floatRange,
                    onValueChange = { value ->
                        AllSettings.resolutionRatio.updateState(value)
                    },
                    onValueChangeFinished = { value ->
                        AllSettings.resolutionRatio.save(value)
                        onRefreshWindowSize()
                    },
                    suffix = "%",
                    color = container,
                    contentColor = content
                )
            }
            AnimatedVisibility(
                visible = AllSettings.resolutionRule.state == ResolutionRule.CUSTOM
            ) {
                CustomResolutionRow(onCommit = onRefreshWindowSize)
            }
        }

        item { SectionTitle("运行") }
        item {
            MenuTextButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.game_menu_option_switch_log),
                onClick = onSwitchLog,
                color = container,
                contentColor = content
            )
        }
        if (enableTerracotta) {
            item {
                MenuTextButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.terracotta_menu),
                    onClick = onOpenTerracottaMenu,
                    color = container,
                    contentColor = content
                )
            }
        }
        item {
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                onClick = onForceClose
            ) {
                Text(
                    text = stringResource(R.string.game_button_force_close),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun CustomResolutionRow(onCommit: () -> Unit) {
    val context = LocalContext.current
    val screenSize = remember(context) { getRealScreenSize(context) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IntInputField(
            modifier = Modifier.weight(1f),
            value = AllSettings.customResolutionWidth.state,
            permitted = customResolutionRange(screenSize.width),
            label = stringResource(R.string.settings_renderer_resolution_custom_width),
            onValueChange = { value ->
                AllSettings.customResolutionWidth.save(value)
                onCommit()
            }
        )
        IntInputField(
            modifier = Modifier.weight(1f),
            value = AllSettings.customResolutionHeight.state,
            permitted = customResolutionRange(screenSize.height),
            label = stringResource(R.string.settings_renderer_resolution_custom_height),
            onValueChange = { value ->
                AllSettings.customResolutionHeight.save(value)
                onCommit()
            }
        )
    }
}