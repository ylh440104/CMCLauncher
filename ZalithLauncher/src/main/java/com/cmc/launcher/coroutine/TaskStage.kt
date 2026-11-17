 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.coroutine

enum class TaskStage {
    /** 预备 */
    PREPARING,
    /** 运行中 */
    RUNNING,
    /** 已完成 */
    COMPLETED
}