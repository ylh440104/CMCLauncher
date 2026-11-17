 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.library

import com.cmc.launcher.R

/**
 * 库使用的协议信息
 * @param name 名称
 * @param raw 协议文本
 */
data class License(
    val name: String,
    val raw: Int
)

/**
 * Apache License 2.0
 * @see <a href="http://www.apache.org/licenses/LICENSE-2.0.txt">Apache License 2.0</a>
 */
val LICENSE_APACHE_2 = License("Apache License 2.0", R.raw.apache_license_2)
