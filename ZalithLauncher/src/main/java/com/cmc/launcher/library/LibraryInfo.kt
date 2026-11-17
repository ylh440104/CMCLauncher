 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.library

/**
 * 依赖库信息
 * @param name 库显示名称
 * @param copyrightInfo 库版权信息
 * @param license 库协议信息
 * @param webUrl 库项目链接
 */
data class LibraryInfo(
    val name: String,
    val copyrightInfo: String?,
    val license: License,
    val webUrl: String
)