 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.addon.platform.modrinth.models

import com.cmc.launcher.game.download.addon.platform.PlatformClasses
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ModrinthProjectType(val platform: PlatformClasses?) {
    @SerialName("mod")
    MOD(PlatformClasses.MOD),

    @SerialName("modpack")
    MODPACK(PlatformClasses.MOD_PACK),

    @SerialName("resourcepack")
    RESOURCEPACK(PlatformClasses.RESOURCE_PACK),

    @SerialName("shader")
    SHADER(PlatformClasses.SHADERS),
}

fun String.mapModrinthType(): ModrinthProjectType? = when (this) {
    "mod" -> ModrinthProjectType.MOD
    "modpack" -> ModrinthProjectType.MODPACK
    "resourcepack" -> ModrinthProjectType.RESOURCEPACK
    "shader" -> ModrinthProjectType.SHADER
    else -> null
}