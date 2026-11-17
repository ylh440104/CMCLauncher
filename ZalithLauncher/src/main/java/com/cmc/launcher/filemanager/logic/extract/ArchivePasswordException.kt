 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.filemanager.logic.extract

/**
 * 解压过程中检测到压缩包需要密码、或密码错误时抛出的异常
 * @param type 错误类型（需要密码 / 密码错误）
 */
class ArchivePasswordException(
    val type: Type,
    message: String = type.message,
    cause: Throwable? = null
) : Exception(message, cause) {
    /** 密码异常类型 */
    enum class Type(val message: String) {
        /** 压缩包已加密，尚未提供密码 */
        REQUIRED("Archive requires a password"),
        /** 提供的密码错误 */
        WRONG("Archive password is incorrect"),
    }
}
