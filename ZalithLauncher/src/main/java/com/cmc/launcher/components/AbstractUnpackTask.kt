 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class AbstractUnpackTask {
    private val _taskMessage = MutableStateFlow<String?>(null)
    /** 描述当前解压任务进度的状态 */
    val taskMessage = _taskMessage.asStateFlow()

    fun updateMessage(message: String?) {
        _taskMessage.update { message }
    }

    abstract suspend fun checkState(): InstallableItem.State
    abstract suspend fun run()
}