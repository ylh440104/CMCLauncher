 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui.control.input

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnKeyboardClosed(
    onClosed: () -> Unit
) {
    val imeVisible = WindowInsets.isImeVisible
    val localView = LocalView.current
    var lastImeVisible by remember { mutableStateOf(imeVisible) }

    LaunchedEffect(imeVisible) {
        if (lastImeVisible && !imeVisible) {
            // IME insets 可能先于弹窗的窗口失焦事件变化，延迟确认以避免误关输入模式
            delay(100.milliseconds)
            if (localView.hasWindowFocus()) {
                onClosed()
            }
        }
        lastImeVisible = imeVisible
    }
}