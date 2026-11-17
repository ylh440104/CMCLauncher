 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.addons.modloader.fabriclike.fabric

import com.cmc.launcher.game.addons.modloader.fabriclike.FabricLikeVersions
import com.cmc.launcher.game.addons.modloader.fabriclike.models.FabricLikeLoader

object FabricVersions : FabricLikeVersions(
    officialUrl = "https://meta.fabricmc.net/v2",
    mirrorUrl = "https://bmclapi2.bangbang93.com/fabric-meta/v2"
) {

    /**
     * 获取 Fabric 列表
     */
    suspend fun fetchFabricLoaderList(mcVersion: String, force: Boolean = false): List<FabricVersion>? {
        val list: List<FabricLikeLoader> = fetchLoaderList(force, "FabricVersions", mcVersion) ?: return null

        return list.map { loader ->
            FabricVersion(
                inherit = mcVersion,
                version = loader.version,
                stable = loader.stable
            )
        }
    }
}