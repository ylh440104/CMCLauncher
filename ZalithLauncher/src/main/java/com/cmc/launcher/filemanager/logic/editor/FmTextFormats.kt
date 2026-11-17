 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.filemanager.logic.editor

/** 可编辑文件的体积上限，超过则拒绝以文本方式打开 */
const val MAX_EDIT_SIZE: Long = 20L * 1024 * 1024

/**
 * 常见的文本格式扩展名（小写）
 * 覆盖文档、标记、配置文件与常见源代码格式
 */
private val KNOWN_TEXT_EXTENSIONS = setOf(
    // 纯文本 / 文档
    "txt", "text", "md", "markdown", "log", "rtf",
    // 标记 / 序列化
    "json", "yaml", "yml", "toml", "xml", "html", "htm", "css",
    // 配置文件
    "ini", "cfg", "conf", "properties", "env", "editorconfig",
    // 常见源代码
    "js", "mjs", "cjs", "ts", "jsx", "tsx", "kt", "kts", "java",
    "c", "h", "cpp", "hpp", "cc", "cxx", "cs", "py", "sh", "bash",
    "zsh", "bat", "cmd", "ps1", "sql", "gradle", "groovy", "rb",
    "go", "rs", "php", "swift", "scala", "lua", "pl", "r",
    // 数据 / 其他
    "csv", "tsv", "diff", "patch", "gitignore", "gitattributes"
)

/**
 * 判断文件名是否属于已知的常见文本格式
 */
fun isKnownTextFile(name: String): Boolean {
    val ext = name.substringAfterLast('.', "").lowercase()
    return ext.isNotEmpty() && ext in KNOWN_TEXT_EXTENSIONS
}
