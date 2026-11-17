package com.cmc.launcher.ui.screens.game.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.cmc.launcher.ui.control.Keyboard
import com.cmc.launcher.ui.control.event.lwjglEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed interface SendKeycodeState {
    data object None : SendKeycodeState
    data object ShowDialog : SendKeycodeState
}

@Composable
fun SendKeycodeOperation(
    operation: SendKeycodeState,
    onChange: (SendKeycodeState) -> Unit,
    lifecycleScope: CoroutineScope
) {
    when (operation) {
        is SendKeycodeState.None -> {}
        is SendKeycodeState.ShowDialog -> {
            val pressedEvents = remember { mutableStateListOf<String>() }
            Keyboard(
                onDismissRequest = {
                    onChange(SendKeycodeState.None)
                },
                onSwitch = { keyString, pressed ->
                    lifecycleScope.launch {
                        if (pressed) pressedEvents.add(keyString)
                        else pressedEvents.remove(keyString)
                        lwjglEvent(keyString, isMouse = false, isPressed = pressed)
                    }
                }
            )
            DisposableEffect(Unit) {
                onDispose {
                    //停止所有的按键事件
                    pressedEvents.forEach { keyString ->
                        lwjglEvent(keyString, isMouse = false, isPressed = false)
                    }
                }
            }
        }
    }
}