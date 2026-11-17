 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui.screens.main.control_editor.edit_widget

import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableWidget

/**
 * 将在组件编辑对话框中编辑的组件
 * @param data 要编辑的组件
 * @param layer 该组件所属的控件层
 */
data class SelectedWidgetData(
    val data: ObservableWidget,
    val layer: ObservableControlLayer
)
