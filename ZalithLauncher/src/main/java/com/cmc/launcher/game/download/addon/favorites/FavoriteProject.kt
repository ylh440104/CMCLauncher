 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.addon.favorites

import android.os.Parcelable
import com.cmc.launcher.game.download.addon.platform.PlatformClasses
import kotlinx.parcelize.Parcelize

/**
 * 收藏的项目本地缓存数据
 * @param projectId 项目在平台上的Id
 * @param iconUrl 项目图标链接
 * @param title 项目名
 * @param description 项目描述
 * @param authors 作者列表
 * @param classes 项目类型
 * @param followTime 收藏时间戳（ms）
 */
@Parcelize
class FavoriteProject(
    val projectId: String,
    val iconUrl: String? = null,
    val title: String,
    val description: String = "",
    val authors: List<String>? = null,
    val classes: PlatformClasses,
    val followTime: Long
) : Parcelable
