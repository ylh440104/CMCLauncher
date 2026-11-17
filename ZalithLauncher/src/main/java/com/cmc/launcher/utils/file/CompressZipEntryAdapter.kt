 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.utils.file

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry

class CompressZipEntryAdapter(val entry: ZipArchiveEntry) : ZipEntryBase {
    override val name: String = entry.name
    override val isDirectory: Boolean = entry.isDirectory
}