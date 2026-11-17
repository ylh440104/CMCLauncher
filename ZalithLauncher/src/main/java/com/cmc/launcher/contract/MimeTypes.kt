 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.contract

import android.webkit.MimeTypeMap

/**
 * 将文件扩展名转化为 MimeType
 */
fun String.extensionToMimeType(): String {
    val extensionMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(this)
    return extensionMimeType ?: "*/*"
}