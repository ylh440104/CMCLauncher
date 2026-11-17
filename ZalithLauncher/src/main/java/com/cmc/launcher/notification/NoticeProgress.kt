 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.notification

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 描述通知内进度条的数据类
 * @param max 最大值
 * @param progress 当前值
 * @param indeterminate 是否为不确定状态
 */
@Parcelize
data class NoticeProgress(
    val max: Int,
    val progress: Int,
    val indeterminate: Boolean = false
): Parcelable
