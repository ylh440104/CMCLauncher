 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.plugin.driver

import com.cmc.launcher.game.plugin.ApkPlugin
import com.cmc.launcher.game.plugin.Plugin

class Driver(
    val id: String,
    appName: String,
    appVersion: String,
    val name: String,
    val summary: String? = null,
    val path: String,
    val isLauncher: Boolean,
): ApkPlugin(
    packageName = id,
    appName = appName,
    appVersion = appVersion
), Plugin {
    override fun getIdentifier(): String = id
    override fun getNativeLibPath(): String = path
}
