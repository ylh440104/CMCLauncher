 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.version.installed.utils

import org.jackhuang.hmcl.util.versioning.GameVersionNumber

/**
 * 判断版本是否大于某个版本
 */
fun String.isBiggerVer(other: String): Boolean {
    return GameVersionNumber.compare(this, other) > 0
}

/**
 * 判断版本是否大于等于某个版本
 */
fun String.isBiggerOrEqualVer(other: String): Boolean {
    return GameVersionNumber.compare(this, other) >= 0
}

/**
 * 判断版本是否小于某个版本
 */
fun String.isLowerVer(other: String): Boolean {
    return GameVersionNumber.compare(this, other) < 0
}

/**
 * 判断版本是否小于等于某个版本
 */
fun String.isLowerOrEqualVer(other: String): Boolean {
    return GameVersionNumber.compare(this, other) <= 0
}