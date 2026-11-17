 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.addons.modloader.fabriclike.quilt

import com.cmc.launcher.game.addons.modloader.fabriclike.FabricLikeVersions
import com.cmc.launcher.game.addons.modloader.fabriclike.models.FabricLikeLoader

object QuiltVersions : FabricLikeVersions("https://meta.quiltmc.org/v3") {

    /**
     * 获取 Quilt 列表
     */
    suspend fun fetchQuiltLoaderList(mcVersion: String, force: Boolean = false): List<QuiltVersion>? {
        val list: List<FabricLikeLoader> = fetchLoaderList(force, "QuiltVersions", mcVersion) ?: return null

        return list.map { loader ->
            QuiltVersion(
                inherit = mcVersion,
                version = loader.version
            )
        }
    }
}