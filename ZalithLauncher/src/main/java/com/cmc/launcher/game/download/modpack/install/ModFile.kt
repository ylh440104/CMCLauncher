 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.modpack.install

import java.io.File

/**
 * 整合包可下载模组文件
 * @param getFile 如果无法临时构建模组下载链接，或者构建模组下载链接过于耗时，则可以在这里进行构建
 */
data class ModFile(
    val outputFile: File? = null,
    val downloadUrls: List<String>? = null,
    val sha1: String? = null,
    val getFile: (suspend () -> ModFile)? = null
)
