 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.modpack.platform.mcbbs

import com.cmc.launcher.game.download.modpack.platform.AbstractPack
import com.cmc.launcher.game.download.modpack.platform.PackPlatform
import com.cmc.launcher.game.download.modpack.platform.SimplePackParser
import com.cmc.launcher.utils.logging.Logger
import java.io.File

private const val TAG = "MCBBSPackMetaParser"

/**
 * MCBBS 整合包解析器，尝试解析 mcbbs.packmeta 来解析这个整合包
 */
object MCBBSPackMetaParser : SimplePackParser<MCBBSManifest>(
    indexFilePath = "mcbbs.packmeta",
    manifestClass = MCBBSManifest::class.java,
    buildPack = { root, manifest ->
        if (!manifest.checkMCBBSManifest()) {
            error("This MCBBS modpack does not provide game version information and cannot be installed!")
        }
        MCBBSPack(root, manifest)
    }
) {
    override suspend fun parse(packFolder: File): AbstractPack? {
        val result = runCatching {
            super.parse(packFolder)
        }.onFailure { th ->
            Logger.debug(TAG, "${getIdentifier()}: Failed to parse the modpack using \"mcbbs.packmeta\", trying \"manifest.json\" instead.", th)
        }.getOrNull() ?: run {
            //如果无法使用 mcbbs.packmeta 解析，则尝试使用 manifest.json
            MCBBSPackManifestParser.parse(packFolder)
        }
        return result
    }

    override fun getIdentifier(): String = PackPlatform.MCBBS.identifier

    /**
     * MCBBS 整合包解析器，尝试解析 manifest.json 来解析这个整合包
     */
    private object MCBBSPackManifestParser : SimplePackParser<MCBBSManifest>(
        indexFilePath = "manifest.json",
        manifestClass = MCBBSManifest::class.java,
        buildPack = { root, manifest ->
            if (!manifest.checkMCBBSManifest()) {
                error("This MCBBS modpack does not provide game version information and cannot be installed!")
            }
            MCBBSPack(root, manifest)
        }
    ) {
        override fun getIdentifier(): String = PackPlatform.MCBBS.identifier
    }
}

/**
 * 检查 MCBBS 整合包是否可用
 * @return 是否可用
 */
private fun MCBBSManifest.checkMCBBSManifest(): Boolean {
    //确保附加内容这一块一定包含 MC 游戏版本
    return addons.any { it.id == "game" }
}