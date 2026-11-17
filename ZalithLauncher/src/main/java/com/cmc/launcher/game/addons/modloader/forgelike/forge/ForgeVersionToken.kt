 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.addons.modloader.forgelike.forge

import com.cmc.launcher.game.addons.modloader.forgelike.forge.ForgeVersionToken.ForgeFile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForgeVersionToken(
    @SerialName("branch")
    val branch: String? = null,
    @SerialName("version")
    val version: String,
    @SerialName("modified")
    val modified: String,
    @SerialName("files")
    val files: List<ForgeFile>
) {
    @Serializable
    data class ForgeFile(
        @SerialName("category")
        val category: String,
        @SerialName("format")
        val format: String,
        @SerialName("hash")
        val hash: String? = null
    )
}

fun ForgeFile.isInstallerJar() = category == "installer" && format == "jar"
fun ForgeFile.isUniversalZip() = category == "universal" && format == "zip"
fun ForgeFile.isClientZip() = category == "client" && format == "zip"