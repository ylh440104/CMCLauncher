 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.addon.platform

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 可用的资源搜索平台
 */
@Serializable
@Parcelize
enum class Platform(val displayName: String): Parcelable {
    @SerialName("CURSEFORGE")
    CURSEFORGE("CurseForge"),
    @SerialName("MODRINTH")
    MODRINTH("Modrinth")
}