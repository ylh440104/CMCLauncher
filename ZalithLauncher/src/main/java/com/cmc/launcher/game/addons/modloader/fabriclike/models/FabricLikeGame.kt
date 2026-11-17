 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.addons.modloader.fabriclike.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FabricLikeGame(
    /** 对应的 Minecraft 版本 */
    @SerialName("version")
    val version: String,
    /** 是否为正式版 */
    @SerialName("stable")
    val stable: Boolean
)
