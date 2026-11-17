 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.utils.logging

/**
 * [Modified from HMCL](https://github.com/HMCL-dev/HMCL/blob/57018be/HMCLCore/src/main/java/org/jackhuang/hmcl/util/logging/LogEvent.java)
 */
data class LogMessage(
    val time: Long,
    val tag: String,
    val level: Level,
    val message: String,
    val throwable: Throwable?
)