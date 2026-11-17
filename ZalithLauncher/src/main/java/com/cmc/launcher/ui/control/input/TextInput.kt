 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui.control.input

import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun HidableInputLayout(
    onSend: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onClose: () -> Unit,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
) {
    var view by remember { mutableStateOf<TouchCharInput?>(null) }
    val localView = LocalView.current

    AndroidView(
        modifier = Modifier
            .alpha(0f)
            .size(1.dp),
        factory = { context ->
            TouchCharInput(context).apply {
                id = View.generateViewId()

                imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN or
                        EditorInfo.IME_FLAG_NO_EXTRACT_UI or
                        EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING or
                        EditorInfo.IME_ACTION_DONE
                inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                        InputType.TYPE_TEXT_VARIATION_FILTER or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE

                setEms(10)

                isFocusableInTouchMode = true
                nextFocusDownId = id
                nextFocusUpId = id
                nextFocusLeftId = id
                nextFocusRightId = id
            }.also { view0 ->
                view = view0.also {
                    it.setListener(
                        object : InputListener {
                            override fun onSend(char: Char) {
                                onSend(char.toString())
                            }
                            override fun onBackspace() {
                                onBackspace()
                            }
                            override fun onEnter() {
                                onEnter()
                                onClose()
                            }
                        }
                    )
                }
            }
        }
    )

    LaunchedEffect(view) {
        val input = view ?: return@LaunchedEffect
        input.enableKeyboard()
    }

    DisposableEffect(Unit) {
        onDispose {
            view?.disableKeyboard()
            keyboardController?.hide()
            view = null
            localView.requestFocus()
        }
    }

    OnKeyboardClosed {
        keyboardController?.hide()
        onClose()
    }
}