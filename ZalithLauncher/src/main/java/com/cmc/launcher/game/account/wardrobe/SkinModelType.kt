 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.account.wardrobe

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

/**
 * 皮肤模型枚举
 */
@Keep
@Parcelize
enum class SkinModelType(
    val string: String,
    val targetParity: Int,
    val modelType: String
): Parcelable {
    /** 未设定 */
    NONE("none", -1, ""),
    /** 粗臂类型 */
    STEVE("wide", 0, "classic"),
    /** 细臂类型 */
    ALEX("slim", 1, "slim")
}