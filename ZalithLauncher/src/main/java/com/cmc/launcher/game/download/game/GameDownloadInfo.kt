 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.game

import com.cmc.launcher.game.addons.modloader.fabriclike.fabric.FabricVersion
import com.cmc.launcher.game.addons.modloader.fabriclike.legacyfabric.LegacyFabricVersion
import com.cmc.launcher.game.addons.modloader.fabriclike.quilt.QuiltVersion
import com.cmc.launcher.game.addons.modloader.forgelike.forge.ForgeVersion
import com.cmc.launcher.game.addons.modloader.forgelike.neoforge.NeoForgeVersion
import com.cmc.launcher.game.addons.modloader.modlike.ModVersion
import com.cmc.launcher.game.addons.modloader.optifine.OptiFineVersion

data class GameDownloadInfo(
    /** Minecraft 版本 */
    val gameVersion: String,
    /** 自定义版本名称 */
    val customVersionName: String,
    val optifine: OptiFineVersion? = null,
    val forge: ForgeVersion? = null,
    val neoforge: NeoForgeVersion? = null,
    val fabric: FabricVersion? = null,
    val fabricAPI: ModVersion? = null,
    val legacyFabric: LegacyFabricVersion? = null,
    val legacyFabricAPI: ModVersion? = null,
    val quilt: QuiltVersion? = null,
    val quiltAPI: ModVersion? = null,
)
