 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.version.saves

import com.cmc.launcher.R

/**
 * @param levelCode 在 level.dat 中存储的值
 */
enum class GameMode(val levelCode: Int, val nameRes: Int) {
    /** 生存模式 */
    SURVIVAL(0, R.string.saves_manage_gamemode_survival),
    /** 创造模式 */
    CREATIVE(1, R.string.saves_manage_gamemode_creative),
    /** 冒险模式 */
    ADVENTURE(2, R.string.saves_manage_gamemode_adventure),
    /** 旁观模式 */
    SPECTATOR(3, R.string.saves_manage_gamemode_spectator)
}