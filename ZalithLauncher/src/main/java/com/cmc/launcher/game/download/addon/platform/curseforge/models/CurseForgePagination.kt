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
class CurseForgePagination(
    /**
     * 查询当前开始的索引
     */
    @SerialName("index")
    val index: Int,

    /**
     * 页面大小
     */
    @SerialName("pageSize")
    val pageSize: Int,

    /**
     * 查询返回的结果数
     */
    @SerialName("resultCount")
    val resultCount: Int,

    /**
     * 与查询匹配的结果总数
     */
    @SerialName("totalCount")
    val totalCount: Long
)