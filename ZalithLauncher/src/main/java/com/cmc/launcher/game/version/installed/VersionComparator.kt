 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.version.installed

import com.cmc.launcher.utils.string.naturalCompare
import org.jackhuang.hmcl.util.versioning.GameVersionNumber

object VersionComparator: Comparator<Version> {
    override fun compare(o1: Version, o2: Version): Int {
        val pinned1 = o1.pinnedState
        val pinned2 = o2.pinnedState

        if (pinned1 != pinned2) {
            return if (pinned1) -1 else 1
        }

        val key1 = o1.getVersionInfo()?.minecraftVersion ?: o1.getVersionName()
        val key2 = o2.getVersionInfo()?.minecraftVersion ?: o2.getVersionName()

        val versionCompare = GameVersionNumber.compare(key1, key2)
        if (versionCompare != 0) {
            return -versionCompare
        }

        return naturalCompare(o1.getVersionName(), o2.getVersionName())
    }
}