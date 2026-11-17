 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.version.mod.reader

import org.apache.commons.compress.archivers.zip.ZipFile
import java.util.zip.ZipFile as JDKZipFile

fun JDKZipFile.tryGetIcon(iconPath: String?): ByteArray? {
    if (iconPath.isNullOrBlank()) return null

    return try {
        getInputStream(getEntry(iconPath))?.readBytes()
    } catch (_: Exception) {
        null //忽略图标读取错误
    }
}

fun ZipFile.tryGetIcon(iconPath: String?): ByteArray? {
    if (iconPath.isNullOrBlank()) return null

    return try {
        getEntry(iconPath)?.let { entry ->
            getInputStream(entry).use { it.readBytes() }
        }
    } catch (_: Exception) {
        null //忽略图标读取错误
    }
}