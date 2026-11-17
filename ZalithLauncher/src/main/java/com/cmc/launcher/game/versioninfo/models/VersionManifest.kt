 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */
package com.cmc.launcher.game.versioninfo.models

import com.google.gson.annotations.SerializedName

data class VersionManifest(
    @SerializedName("latest")
    val latest: Latest,
    @SerializedName("versions")
    val versions: List<Version>
) {
    data class Latest(
        @SerializedName("release")
        val release: String,
        @SerializedName("snapshot")
        val snapshot: String
    )

    data class Version(
        @SerializedName("id")
        val id: String,
        @SerializedName("type")
        val type: String,
        @SerializedName("url")
        val url: String,
        @SerializedName("time")
        val time: String,
        @SerializedName("releaseTime")
        val releaseTime: String,
        @SerializedName("sha1")
        val sha1: String? = null,
        @SerializedName("complianceLevel")
        val complianceLevel: Int? = null
    )
}
