 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.renderer

import com.cmc.launcher.game.plugin.Plugin
import com.cmc.launcher.game.version.installed.Version
import com.cmc.launcher.game.version.mod.LocalMod
import com.cmc.launcher.game.version.mod.isEnabled
import com.cmc.launcher.path.PathManager
import com.cmc.launcher.utils.logging.Logger

private const val TAG = "SimpleFPEWrapper"

/**
 * @return 获取 SimpleFPEWrapper 原生库的绝对路径
 */
fun getSfpewLibraryPath(): String = "${PathManager.DIR_NATIVE_LIB}/libSimpleFPEWrapper.so"

/**
 * @return 判断启动 [version] 时是否已为 [renderer] 配置 SFPEW
 */
fun isSfpewConfiguredFor(renderer: RendererInterface, version: Version): Boolean {
    return if (renderer is Plugin) {
        version.isSfpewPluginLayerEnabled()
    } else {
        renderer.supportsSfpewByDefault()
    }
}

/**
 * 判断启动 [version] 时是否应启用 SFPEW
 *
 * [launchConfig] 为启动前解析好的渲染器配置，其开关声明优先于渲染器默认
 * 版本中存在 FPE 模拟模组时禁用兼容层，避免功能冲突
 */
fun isSfpewEnabledFor(
    renderer: RendererInterface,
    version: Version,
    launchConfig: RendererLaunchConfig? = null
): Boolean =
    !version.fpeEmulatorModDetected &&
        (launchConfig?.sfpewLayerEnabled ?: isSfpewConfiguredFor(renderer, version))

/**
 * 检测 [mods] 中已启用的 FPE 模拟模组
 * @return 检测到至少一个已启用的 FPE 模拟模组时返回 `true`
 */
fun detectFpeEmulatorMods(mods: List<LocalMod>): Boolean {
    val fpeEmulatorModIds = setOf("angelica", "actinium")

    val detectedIds = mods.filter {
        it.file.isEnabled() && it.id in fpeEmulatorModIds
    }.mapTo(mutableSetOf()) { it.id }

    if (detectedIds.isNotEmpty()) {
        Logger.info(
            TAG,
            "FPE emulator mod detected (${detectedIds.joinToString()}), compatibility layer disabled"
        )
    }
    return detectedIds.isNotEmpty()
}