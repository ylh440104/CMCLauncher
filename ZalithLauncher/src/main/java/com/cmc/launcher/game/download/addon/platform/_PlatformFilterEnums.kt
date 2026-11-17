 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.addon.platform

import android.os.Parcelable
import com.cmc.launcher.game.download.addon.platform.curseforge.models.CurseForgeClassID
import com.cmc.launcher.game.download.addon.platform.modrinth.models.ProjectTypeFacet
import com.cmc.launcher.game.version.installed.VersionFolders

interface PlatformFilterCode {
    fun getDisplayName(): Int
    fun index(): Int
}

interface PlatformDisplayLabel {
    fun getDisplayName(): String
    fun index(): Int
}

interface ModLoaderDisplayLabel: Parcelable, PlatformDisplayLabel

enum class PlatformClasses(
    val curseforge: CurseForgeClassID,
    val modrinth: ProjectTypeFacet?,
    val versionFolder: VersionFolders
) {
    MOD(
        curseforge = CurseForgeClassID.MOD,
        modrinth = ProjectTypeFacet.MOD,
        versionFolder = VersionFolders.MOD
    ),
    MOD_PACK(
        curseforge = CurseForgeClassID.MOD_PACK,
        modrinth = ProjectTypeFacet.MODPACK,
        versionFolder = VersionFolders.NONE
    ),
    RESOURCE_PACK(
        curseforge = CurseForgeClassID.RESOURCE_PACK,
        modrinth = ProjectTypeFacet.RESOURCE_PACK,
        versionFolder = VersionFolders.RESOURCE_PACK
    ),
    SAVES(
        curseforge = CurseForgeClassID.SAVES,
        modrinth = null,
        versionFolder = VersionFolders.SAVES
    ),
    SHADERS(
        curseforge = CurseForgeClassID.SHADERS,
        modrinth = ProjectTypeFacet.SHADER,
        versionFolder = VersionFolders.SHADERS
    )
}