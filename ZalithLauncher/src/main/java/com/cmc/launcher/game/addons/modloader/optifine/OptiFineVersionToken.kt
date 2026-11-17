 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.addons.modloader.optifine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OptiFineVersionToken(
    @SerialName("mcversion")
    val mcVersion: String,
    @SerialName("type")
    val type: String,
    @SerialName("patch")
    val patch: String,
    @SerialName("filename")
    val fileName: String,
    @SerialName("forge")
    val forge: String? = null
)