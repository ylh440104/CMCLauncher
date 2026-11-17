 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui.screens.main.control_editor.style_cabinet

/**
 * 打开样式收藏柜的来源
 */
enum class OpenCabinetSource {
    /**
     * 通过控制布局菜单打开收藏柜
     */
    Menu,

    /**
     * 通过控件样式对话框打开收藏柜
     */
    ControlStyle,

    /**
     * 通过摇杆样式列表对话框打开收藏柜
     */
    JoystickStyle,
}