 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.addon.platform

data class PlatformSearchFilter(
    val searchName: String = "",
    val gameVersion: String = "",
    val sortField: PlatformSortField = PlatformSortField.RELEVANCE,
    val categories: List<PlatformFilterCode> = emptyList(),
    val modloader: PlatformDisplayLabel? = null,
    val index: Int = 0,
    val limit: Int = 20
)