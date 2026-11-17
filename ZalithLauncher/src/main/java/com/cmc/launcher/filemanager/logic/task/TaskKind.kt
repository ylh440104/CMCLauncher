 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.filemanager.logic.task

/** 任务种类，同一时间仅允许一个任务运行 */
enum class TaskKind {
    LIST,
    SEARCH,
    COPY,
    MOVE,
    DELETE,
    COMPRESS,
    EXTRACT,
    IMPORT,
    TRASH_RESTORE,
    TRASH_PURGE,
    TRASH_CLEAR;

    /** 是否应弹出任务对话框 */
    val shouldShowProgressDialog: Boolean
        get() = this != LIST && this != SEARCH
}