 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.version.mod

import com.cmc.launcher.game.version.mod.reader.FabricModMetadataReader
import com.cmc.launcher.game.version.mod.reader.ForgeNewModMetadataReader
import com.cmc.launcher.game.version.mod.reader.ForgeOldModMetadataReader
import com.cmc.launcher.game.version.mod.reader.LiteModMetadataReader
import com.cmc.launcher.game.version.mod.reader.OptiFineModReader
import com.cmc.launcher.game.version.mod.reader.PackMcMetadataReader
import com.cmc.launcher.game.version.mod.reader.QuiltModMetadataReader
import java.io.File

interface ModMetadataReader {
    suspend fun fromLocal(modFile: File): LocalMod
}

private val NORMAL_READERS = arrayOf(
    OptiFineModReader,
    ForgeOldModMetadataReader,
    ForgeNewModMetadataReader,
    FabricModMetadataReader,
    QuiltModMetadataReader,
    PackMcMetadataReader
)

val MOD_READERS = mapOf<String, Array<ModMetadataReader>>(
    "zip" to NORMAL_READERS,
    "jar" to NORMAL_READERS,
    "litemod" to arrayOf(
        LiteModMetadataReader
    )
)