 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.account.microsoft

enum class AsyncStatus {
    GETTING_ACCESS_TOKEN,
    GETTING_XBL_TOKEN,
    GETTING_XSTS_TOKEN,
    AUTHENTICATE_MINECRAFT,
    VERIFY_GAME_OWNERSHIP,
    GETTING_PLAYER_PROFILE
}