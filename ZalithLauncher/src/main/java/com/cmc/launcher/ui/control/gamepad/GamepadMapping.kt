 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui.control.gamepad

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 手柄与键盘映射
 * @param key 手柄键值
 * @param targetsInGame 目标键盘映射（游戏内）
 * @param targetsInMenu 目标键盘映射（菜单内）
 */
@Parcelize
data class GamepadMapping(
    val key: Int,
    val dpadDirection: DpadDirection?,
    val targetsInGame: Set<String> = emptySet(),
    val targetsInMenu: Set<String> = emptySet()
): Parcelable