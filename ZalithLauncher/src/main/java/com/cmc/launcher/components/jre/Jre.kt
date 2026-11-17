 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.components.jre

import com.cmc.launcher.R

enum class Jre(val jreName: String, val jrePath: String, val summary: Int, val majorVersion: Int) {
    JRE_8("Internal-8", "runtimes/jre-8", R.string.unpack_screen_jre8, 8),
    JRE_17("Internal-17", "runtimes/jre-17", R.string.unpack_screen_jre17, 17),
    JRE_21("Internal-21", "runtimes/jre-21", R.string.unpack_screen_jre21, 21),
    JRE_25("Internal-25", "runtimes/jre-25", R.string.unpack_screen_jre25, 25)
}