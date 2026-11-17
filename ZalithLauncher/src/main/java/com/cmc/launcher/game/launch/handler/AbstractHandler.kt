package com.cmc.launcher.game.launch.handler

import android.view.KeyEvent
import android.view.Surface
import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.cmc.launcher.game.launch.Launcher
import com.cmc.launcher.ui.control.input.TextInputMode
import com.cmc.launcher.viewmodel.ErrorViewModel
import com.cmc.launcher.viewmodel.EventViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class AbstractHandler(
    val type: HandlerType,
    protected val errorViewModel: ErrorViewModel,
    protected val eventViewModel: EventViewModel,
    val launcher: Launcher,
    val onExit: (code: Int) -> Unit
) {
    var mIsSurfaceDestroyed: Boolean = false
    open val inputArea: StateFlow<IntRect?> = MutableStateFlow(null)

    @CallSuper
    open suspend fun execute(
        surface: Surface,
        screenSize: IntSize,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.Default) {
            val code = launcher.launch(screenSize)
            onExit(code)
        }
    }

    abstract fun onPause()
    abstract fun onResume()
    abstract fun onDestroy()
    abstract fun onGraphicOutput()
    abstract fun shouldIgnoreKeyEvent(event: KeyEvent): Boolean
    abstract fun sendMouseRight(isPressed: Boolean)

    @Composable
    abstract fun ComposableLayout(
        textInputMode: TextInputMode
    )
}