 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.version.mod

import android.os.Parcelable
import com.cmc.launcher.game.download.addon.platform.Platform
import com.cmc.launcher.game.download.addon.platform.PlatformVersion
import com.cmc.launcher.game.download.addon.platform.curseforge.models.CurseForgeFile
import com.cmc.launcher.game.download.addon.platform.modrinth.models.ModrinthVersion
import kotlinx.parcelize.Parcelize

/**
 * 本地模组文件在平台上匹配到的安装信息
 * @param platform 所属平台
 * @param projectId 平台项目ID
 * @param versionId 平台版本（文件）ID
 * @param versionName 平台版本号
 * @param notFound 平台上是否存在该项目；作为指纹未命中时的负缓存标记
 */
@Parcelize
data class InstalledMod(
    val platform: Platform,
    val projectId: String,
    val versionId: String,
    val versionName: String,
    val notFound: Boolean = false
) : Parcelable

/**
 * 指纹匹配到的平台版本转换为本地安装信息
 */
fun PlatformVersion.toInstalledMod(): InstalledMod = when (this) {
    is ModrinthVersion -> InstalledMod(
        platform = Platform.MODRINTH,
        projectId = projectId,
        versionId = id,
        versionName = versionNumber
    )
    is CurseForgeFile -> InstalledMod(
        platform = Platform.CURSEFORGE,
        projectId = modId.toString(),
        versionId = id.toString(),
        versionName = displayName
    )
    else -> error("Unknown version type: $this")
}
