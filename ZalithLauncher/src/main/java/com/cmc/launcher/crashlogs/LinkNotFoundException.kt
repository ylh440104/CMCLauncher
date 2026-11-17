 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.crashlogs

/**
 * API 站点返回的链接不可用、不存在
 */
class LinkNotFoundException : RuntimeException(
    "Unable to find an available link from the data returned by the remote end"
)