 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.version.mod

import android.os.Parcelable
import com.cmc.launcher.game.download.addon.platform.ModLoaderDisplayLabel
import com.cmc.launcher.game.download.addon.platform.Platform
import kotlinx.parcelize.Parcelize

/**
 * 模组在平台上对应的文件
 * @param id 文件ID
 * @param platform 所属平台
 * @param datePublished 发布日期
 */
@Parcelize
class ModFile(
    val id: String,
    val projectId: String,
    val platform: Platform,
    val loaders: Array<ModLoaderDisplayLabel>,
    val datePublished: String
): Parcelable