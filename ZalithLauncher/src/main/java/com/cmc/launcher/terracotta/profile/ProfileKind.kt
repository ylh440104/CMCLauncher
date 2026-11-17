 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.terracotta.profile

import com.google.gson.annotations.SerializedName
import com.cmc.launcher.R

enum class ProfileKind(val textRes: Int) {
    @SerializedName("HOST")
    HOST(R.string.terracotta_player_kind_host),

    @SerializedName("LOCAL")
    LOCAL(R.string.terracotta_player_kind_local),

    @SerializedName("GUEST")
    GUEST(R.string.terracotta_player_kind_guest)
}
