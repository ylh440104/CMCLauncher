 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.addons.modloader.forgelike.neoforge.models

import com.cmc.launcher.game.addons.modloader.forgelike.neoforge.NeoForgeVersion
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class BMCLAPIMaven(
    @SerialName("files")
    val files: List<File>,
    @SerialName("name")
    val name: String = "",
    @SerialName("type")
    val type: String = "",
    @Transient
    val isLegacy: Boolean = false
): NeoForgeMergeableMaven<BMCLAPIMaven> {
    @Serializable
    data class File(
        @SerialName("contentLength")
        val contentLength: Int? = null,
        @SerialName("contentType")
        val contentType: String? = null,
        @SerialName("lastModifiedTime")
        val lastModifiedTime: Double? = null,
        @SerialName("name")
        val name: String,
        @SerialName("type")
        val type: String
    )

    override fun plus(maven: BMCLAPIMaven): List<NeoForgeVersion> {
        return this.files.mapNotNull { file ->
            val versionId = file.name
            if (file.type != "DIRECTORY" || versionId.contains("maven", true)) return@mapNotNull null
            if (isVersionInvalid(versionId)) return@mapNotNull null
            NeoForgeVersion(versionId, this.isLegacy)
        } + maven.files.mapNotNull { file ->
            val versionId = file.name
            if (file.type != "DIRECTORY" || versionId.contains("maven", true)) return@mapNotNull null
            if (isVersionInvalid(versionId)) return@mapNotNull null
            NeoForgeVersion(versionId, maven.isLegacy)
        }
    }
}