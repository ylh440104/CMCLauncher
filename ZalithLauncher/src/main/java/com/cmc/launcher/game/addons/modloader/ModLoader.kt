 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.addons.modloader

/**
 * 模组加载器/模组类别枚举
 * @param isLoader 该类别是一个模组加载器
 * @param isApiMod 该类别是一个 API 模组
 * @param autoDownloadable 该模组加载器是启动器支持自动安装的加载器
 */
enum class ModLoader(
    val displayName: String,
    val isLoader: Boolean = true,
    val isApiMod: Boolean = false,
    val autoDownloadable: Boolean = true
) {
    UNKNOWN(displayName = "", isLoader = false, autoDownloadable = false),
    OPTIFINE(displayName = "OptiFine", isLoader = false, autoDownloadable = false),
    FORGE(displayName = "Forge"),
    NEOFORGE(displayName = "NeoForge"),

    FABRIC(displayName = "Fabric"),
    FABRIC_API(displayName = "Fabric API", isLoader = false, isApiMod = true, autoDownloadable = false),

    LEGACY_FABRIC(displayName = "Legacy Fabric"),
    LEGACY_FABRIC_API(displayName = "Legacy Fabric API", isLoader = false, isApiMod = true, autoDownloadable = false),

    BABRIC(displayName = "Babric", autoDownloadable = false),
    BABRIC_API(displayName = "Babric API", isLoader = false, isApiMod = true, autoDownloadable = false),

    QUILT(displayName = "Quilt"),
    QUILT_API(displayName = "Quilted Fabric API", isLoader = false, isApiMod = true, autoDownloadable = false),

    LITE_LOADER(displayName = "LiteLoader", autoDownloadable = false),

    PACK(displayName = "Pack", isLoader = false, autoDownloadable = false)
}