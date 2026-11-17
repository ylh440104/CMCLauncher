 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.version.multiplayer.description

import com.cmc.launcher.utils.string.stripColorCodes

/**
 * 以文本形式独立存储的服务器描述
 * @param value 文本信息
 */
data class StringDescription(
    val value: String
): ServerDescription {
    override fun toString(): String {
        return "StringDescription:\n${value.stripColorCodes()}"
    }
}