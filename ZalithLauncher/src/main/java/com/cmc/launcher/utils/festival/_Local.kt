 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.utils.festival

import androidx.compose.runtime.compositionLocalOf

/**
 * 提供今天的节日信息
 */
val LocalFestivals = compositionLocalOf<List<Festival>> {
    error("No Festivals provided")
}