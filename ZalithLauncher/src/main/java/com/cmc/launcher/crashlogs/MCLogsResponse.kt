 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.crashlogs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MCLogsResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("url")
    val url: String? = null,
    @SerialName("id")
    val id: String? = null,
    @SerialName("error")
    val error: String? = null
)