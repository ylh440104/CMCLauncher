 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.version.installed.cleanup

import java.io.File
import java.io.IOException

/**
 * 文件清理失败异常
 * @param files 无法清理的文件
 */
class CleanFailedException(
    val files: List<File>
) : IOException()