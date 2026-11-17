 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.plugin

/**
 * 启用其已识别到的软件插件
 * @param packageName 包名
 * @param appName 软件名称
 * @param appVersion 应用版本
 */
open class ApkPlugin(
    val packageName: String,
    val appName: String,
    val appVersion: String
)