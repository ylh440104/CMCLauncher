 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.coroutine

import androidx.annotation.DrawableRes
import com.cmc.launcher.ui.AndroidStringText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

data class TitledTask(
    val title: AndroidStringText,
    @field:DrawableRes
    val runningIcon: Int? = null,
    val task: Task
)

fun MutableList<TitledTask>.addTask(
    title: AndroidStringText,
    @DrawableRes
    icon: Int? = null,
    id: String? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    action: suspend CoroutineScope.(Task) -> Unit
) {
    add(
        TitledTask(
            title = title,
            runningIcon = icon,
            task = Task.runTask(id = id, dispatcher = dispatcher, task = action)
        )
    )
}

fun MutableList<TitledTask>.addTask(
    title: AndroidStringText,
    @DrawableRes
    icon: Int? = null,
    task: Task
) {
    add(
        TitledTask(
            title = title,
            runningIcon = icon,
            task = task
        )
    )
}