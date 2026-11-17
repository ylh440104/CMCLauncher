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
data class FabricLikeLoader(
    @SerialName("separator")
    val separator: String,
    @SerialName("build")
    val build: Int,
    @SerialName("maven")
    val maven: String,
    /** 加载器版本 */
    @SerialName("version")
    val version: String,
    /** 版本状态: true 为稳定版 (Quilt忽略此值) */
    @SerialName("stable")
    val stable: Boolean = true
)