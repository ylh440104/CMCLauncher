 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.version.mod.meta

import com.google.gson.annotations.SerializedName

// from https://github.com/HMCL-dev/HMCL/commit/2c428faa9540e8666ce3cec07ff284cb53eab34a
class ForgeOldModMetadataList(
    @SerializedName("modListVersion")
    val modListVersion: Int?,
    @SerializedName("modList")
    val modList: List<ForgeOldModMetadata>?
)