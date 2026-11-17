 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.addon.platform.curseforge.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CurseForgeVersions(
    /** 响应数据 */
    @SerialName("data")
    val data: Array<CurseForgeFile>,

    /** 响应分页信息 */
    @SerialName("pagination")
    val pagination: CurseForgePagination
)