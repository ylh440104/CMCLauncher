 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.addons.modloader.fabriclike.legacyfabric

import com.cmc.launcher.game.addons.modloader.ModLoader
import com.cmc.launcher.game.addons.modloader.fabriclike.FabricLikeVersion

class LegacyFabricVersion(
    inherit: String,
    version: String,
    stable: Boolean
): FabricLikeVersion(
    inherit = inherit,
    loaderName = ModLoader.LEGACY_FABRIC_API.displayName,
    version = version,
    stable = stable
) {
    override val loaderUrl: String
        get() = "${LegacyFabricVersions.officialUrl}/versions/loader"
}