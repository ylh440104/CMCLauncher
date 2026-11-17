 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.modpack.platform

import com.cmc.launcher.game.download.modpack.platform.curseforge.CurseForgePackParser
import com.cmc.launcher.game.download.modpack.platform.mcbbs.MCBBSPackMetaParser
import com.cmc.launcher.game.download.modpack.platform.modrinth.ModrinthPackParser
import com.cmc.launcher.game.download.modpack.platform.multimc.MultiMCPackParser
import java.io.File

/**
 * 整合包解析通用接口，用于尝试解析已解压的整合包的格式
 */
interface PackParser {
    /**
     * 尝试解析已经解压的整合包的格式
     * @param packFolder 解压后的整合包文件夹
     */
    suspend fun parse(packFolder: File): AbstractPack?

    /**
     * 获取该解析器的标识
     */
    fun getIdentifier(): String
}

/**
 * 启动器所有支持的整合包格式解析器
 */
val ALL_PACK_PARSER = listOf(
    CurseForgePackParser,
    ModrinthPackParser,
    MultiMCPackParser,
    MCBBSPackMetaParser
)