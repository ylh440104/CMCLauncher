 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.control

import com.movtery.layer_controller.observable.ObservableControlLayout
import java.io.File

/**
 * 控件管理数据类
 */
data class ControlData(
    val file: File,
    val controlLayout: ObservableControlLayout,
    val isSupport: Boolean = true
)