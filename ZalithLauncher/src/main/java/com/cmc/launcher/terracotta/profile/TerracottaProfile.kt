 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.terracotta.profile

import com.google.gson.annotations.SerializedName

class TerracottaProfile private constructor(
    @SerializedName("machine_id")
    val machineID: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("vendor")
    val vendor: String,
    @SerializedName("kind")
    val type: ProfileKind
) {
    override fun toString(): String {
        return "TerracottaProfile:{machineID=$machineID,name=$name,vendor=$vendor,type=$type}"
    }
}
