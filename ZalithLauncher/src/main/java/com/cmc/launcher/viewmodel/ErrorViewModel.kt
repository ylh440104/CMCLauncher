 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.cmc.launcher.R
import com.cmc.launcher.ui.AndroidStringText
import com.cmc.launcher.ui.theme.showThemed
import com.cmc.launcher.ui.toAndroidString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ErrorViewModel : ViewModel() {
    private val _errorEvents = MutableSharedFlow<ThrowableMessage>()
    val errorEvents: SharedFlow<ThrowableMessage> = _errorEvents

    fun showError(message: ThrowableMessage) {
        viewModelScope.launch {
            _errorEvents.emit(message)
        }
    }

    /**
     * 通用的错误信息展示对话框
     */
    suspend fun showErrorDialog(
        context: Context,
        tm: ThrowableMessage
    ) {
        withContext(Dispatchers.Main) {
            //展示一个一次性的错误信息对话框
            MaterialAlertDialogBuilder(context)
                .setTitle(tm.title.toAndroidString(context))
                .setMessage(tm.message.toAndroidString(context))
                .setPositiveButton(R.string.generic_confirm) { dialog, _ ->
                    dialog.dismiss()
                }.setCancelable(false)
                .showThemed()
        }
    }

    data class ThrowableMessage(val title: AndroidStringText, val message: AndroidStringText)
}