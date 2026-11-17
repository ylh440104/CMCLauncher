 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.versioninfo.models

import com.google.gson.annotations.SerializedName

enum class OperatingSystem(val identifier: String) {
    /**
     * Microsoft Windows.
     */
    @SerializedName("windows")
    Windows("windows"),

    /**
     * Linux and Unix like OS, including Solaris.
     * (Android here)
     */
    @SerializedName("linux")
    Linux("linux"),

    /**
     * Mac OS X.
     */
    @SerializedName("osx")
    MacOS("osx"),

    /**
     * Unknown operating system.
     */
    @SerializedName("universal")
    Unknown("universal")
}