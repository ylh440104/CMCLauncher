 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.version.mod.meta

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class QuiltModMetadata(
    @SerializedName("schema_version")
    val schemaVersion: Int,
    @SerializedName("quilt_loader")
    val quiltLoader: QuiltLoader
) {
    data class QuiltLoader(
        @SerializedName("id")
        val id: String,
        @SerializedName("version")
        val version: String,
        @SerializedName("metadata")
        val metadata: Metadata
    ) {
        data class Metadata(
            @SerializedName("name")
            val name: String,
            @SerializedName("description")
            val description: String,
            @SerializedName("contributors")
            val contributors: JsonObject? = null,
            @SerializedName("icon")
            val icon: String? = null,
            @SerializedName("contact")
            val contact: JsonObject? = null
        )
    }
}