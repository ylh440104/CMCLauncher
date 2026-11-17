 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.version.mod

import android.os.Parcelable
import com.cmc.launcher.game.download.addon.platform.Platform
import kotlinx.parcelize.Parcelize

/**
 * 展示到模组管理页面的项目信息
 */
@Parcelize
class ModProject(
    val id: String,
    val platform: Platform,
    val iconUrl: String? = null,
    val title: String,
    val slug: String
): Parcelable