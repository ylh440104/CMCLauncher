 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.modpack.platform.modrinth

import com.cmc.launcher.game.download.modpack.platform.PackPlatform
import com.cmc.launcher.game.download.modpack.platform.SimplePackParser

/**
 * Modrinth 整合包解析器，用于尝试以 .mrpack 的格式解析整合包
 */
object ModrinthPackParser : SimplePackParser<ModrinthManifest>(
    indexFilePath = "modrinth.index.json",
    manifestClass = ModrinthManifest::class.java,
    buildPack = { root, manifest ->
        ModrinthPack(root = root, manifest = manifest)
    }
) {
    override fun getIdentifier(): String = PackPlatform.Modrinth.identifier
}