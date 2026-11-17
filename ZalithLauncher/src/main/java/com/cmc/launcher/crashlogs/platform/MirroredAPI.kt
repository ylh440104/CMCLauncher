 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.crashlogs.platform

import com.cmc.launcher.crashlogs.AbstractAPI

object MirroredAPI: AbstractAPI(
    api = "https://api.logshare.cn/1/log",
    root = "https://logshare.cn"
)