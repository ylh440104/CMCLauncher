 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.bridge

import androidx.compose.ui.input.pointer.PointerIcon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.view.PointerIcon as NativePointerIcon

object ZLBridgeStates {

    private val _cursorMode = MutableStateFlow(CURSOR_ENABLED)
    /** 状态：指针模式（启用、禁用） */
    val cursorMode = _cursorMode.asStateFlow()

    /**
     * 变更指针模式
     */
    @JvmStatic
    fun changeCursorMode(mode: Int) {
        require(mode in 0..1)
        this._cursorMode.update { mode }
    }

    private val _cursorShape = MutableStateFlow(CursorShape.Arrow)
    /** 状态：指针形状 */
    val cursorShape = _cursorShape.asStateFlow()

    /**
     * 变更指针形状
     */
    @JvmStatic
    fun changeCursorShape(shape: CursorShape) {
        _cursorShape.update { shape }
    }

    @JvmStatic
    private val _windowChangeKey = MutableStateFlow(false)
    /** 状态：窗口变更刷新key */
    val windowChangeKey = _windowChangeKey.asStateFlow()

    fun onWindowChange() {
        this._windowChangeKey.update { old -> old.not() }
    }
}

/** 指针:启用 */
const val CURSOR_ENABLED = 1
/** 指针:禁用 */
const val CURSOR_DISABLED = 0

/**
 * 指针形状（目前仅支持箭头、输入、手型）
 */
enum class CursorShape(
    val composeIcon: PointerIcon
) {
    /**
     * 箭头
     */
    Arrow(PointerIcon.Default),

    /**
     * 输入
     */
    IBeam(PointerIcon.Text),

    /**
     * 手形
     */
    Hand(PointerIcon.Hand),

    /**
     * 十字
     */
    CrossHair(PointerIcon.Crosshair),

    /**
     * 调整大小（上下）
     */
    ResizeNS(PointerIcon(NativePointerIcon.TYPE_VERTICAL_DOUBLE_ARROW)),

    /**
     * 调整大小（左右）
     */
    ResizeEW(PointerIcon(NativePointerIcon.TYPE_HORIZONTAL_DOUBLE_ARROW)),

    /**
     * 调整大小（全部方向）
     */
    ResizeAll(PointerIcon(NativePointerIcon.TYPE_ALL_SCROLL)),

    /**
     * 禁止/无效操作
     */
    NotAllowed(PointerIcon(NativePointerIcon.TYPE_NO_DROP))
}