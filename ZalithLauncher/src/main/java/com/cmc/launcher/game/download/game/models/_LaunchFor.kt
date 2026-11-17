 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.game.models

import com.cmc.launcher.game.addons.modloader.ModLoader
import com.cmc.launcher.game.addons.modloader.fabriclike.FabricLikeVersion
import com.cmc.launcher.game.addons.modloader.forgelike.ForgeLikeVersion
import com.cmc.launcher.game.addons.modloader.optifine.OptiFineVersion

/**
 * 将 OptiFine 版本转换为 LaunchFor Info
 */
fun OptiFineVersion.toLaunchForInfo(): LaunchFor.Info {
    return LaunchFor.Info(
        version = this.version, // 存储规范版本串，便于修改版本时匹配
        name = ModLoader.OPTIFINE.displayName
    )
}

/**
 * 将 Forge Like 版本转换为 LaunchFor Info
 */
fun ForgeLikeVersion.toLaunchForInfo(): LaunchFor.Info {
    return LaunchFor.Info(
        version = this.versionName,
        name = this.loaderName
    )
}

/**
 * 将 Fabric Like 版本转换为 LaunchFor Info
 */
fun FabricLikeVersion.toLaunchForInfo(): LaunchFor.Info {
    return LaunchFor.Info(
        version = this.version,
        name = this.loaderName
    )
}