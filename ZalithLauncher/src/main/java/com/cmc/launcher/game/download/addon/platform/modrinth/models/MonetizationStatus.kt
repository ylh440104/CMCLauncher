 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.addon.platform.modrinth.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MonetizationStatus {
    @SerialName("monetized")
    MONETIZED,

    @SerialName("demonetized")
    DEMONETIZED,

    @SerialName("force-demonetized")
    FORCE_DEMONETIZED
}