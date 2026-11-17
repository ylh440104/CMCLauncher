 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.version.export

import android.content.Context
import com.cmc.launcher.coroutine.TitledTask
import com.cmc.launcher.game.version.installed.Version
import java.io.File

abstract class AbstractExporter(
    val type: PackType
) {
    /**
     * 构建所需的导出任务
     */
    abstract fun MutableList<TitledTask>.buildTasks(
        context: Context,
        version: Version,
        info: ExportInfo,
        tempPath: File
    )

    /**
     * 整合包文件后缀
     */
    abstract val fileSuffix: String

    protected fun generateTargetRoot(
        file: File,
        rootPath: String,
        targetPath: String
    ): File {
        return File(targetPath, relativePath(file, rootPath))
    }

    /**
     * 获取一个文件的相对路径
     */
    protected fun relativePath(
        file: File,
        rootPath: String
    ): String {
        return file.absolutePath
            .removePrefix(rootPath)
            .removePrefix("/")
    }
}