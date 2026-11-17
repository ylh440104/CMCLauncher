 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.version.installed

import com.cmc.launcher.game.addons.modloader.ModLoader
import com.cmc.launcher.game.version.installed.VersionType.MODLOADERS
import com.cmc.launcher.game.version.installed.VersionType.UNKNOWN
import com.cmc.launcher.game.version.installed.VersionType.VANILLA

/**
 * 版本类型，区分原版、模组加载器
 */
enum class VersionType {
    /**
     * 原版
     */
    VANILLA,

    /**
     * 带有模组加载器
     */
    MODLOADERS,

    /**
     * 不清楚，无法判断
     */
    UNKNOWN
}

private val loaders = ModLoader.entries.filter { it.isLoader }

/**
 * 通过版本信息，尝试识别版本类型
 */
fun VersionInfo?.getVersionType(): VersionType {
    val loader = this?.primaryLoader
    return when {
        loader == null || loader.loader == ModLoader.OPTIFINE -> VANILLA
        loader.loader in loaders -> MODLOADERS
        else -> UNKNOWN
    }
}

