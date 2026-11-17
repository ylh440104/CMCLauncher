 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.setting.enums

import com.cmc.launcher.R

/**
 * 游戏内帧率的展示模式
 */
enum class FpsDisplayMode(val nameRes: Int) {
    NUMBER(R.string.game_menu_option_fps_display_mode_number),
    CHART(R.string.game_menu_option_fps_display_mode_chart)
}
