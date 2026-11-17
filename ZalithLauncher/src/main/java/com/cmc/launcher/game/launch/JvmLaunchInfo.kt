 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.launch

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

/**
 * Jvm启动配置
 * @param jvmArgs Java虚拟机参数
 * @param jreName 使用的Java名称
 * @param useUserJvm 是否允许使用用户指定的参数
 */
@Keep
@Parcelize
data class JvmLaunchInfo(
    val jvmArgs: String,
    val jreName: String? = null,
    val userHome: String? = null,
    val useUserJvm: Boolean = true,
) : Parcelable