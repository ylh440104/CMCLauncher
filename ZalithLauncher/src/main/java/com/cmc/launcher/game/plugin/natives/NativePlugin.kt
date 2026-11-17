 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.plugin.natives

import com.cmc.launcher.game.plugin.ApkPlugin
import com.cmc.launcher.game.plugin.Plugin

class NativePlugin(
    packageName: String,
    appName: String,
    appVersion: String,
    val displayName: String,
    val minMCVer: String? = null,
    val maxMCVer: String? = null,
    val path: String,
    val envList: List<String>
): ApkPlugin(
    packageName = packageName,
    appName = appName,
    appVersion = appVersion
), Plugin {
    override fun getIdentifier(): String = packageName
    override fun getNativeLibPath(): String = path
}
