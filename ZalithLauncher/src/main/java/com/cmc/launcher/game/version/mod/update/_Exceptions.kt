 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.version.mod.update

/**
 * 等待用户确认模组更新时，如果用户选择取消，则抛出[ModUpdateCancelledException]异常，终止任务
 */
class ModUpdateCancelledException : RuntimeException("Update cancelled by user")

/**
 * 当所有模组都是最新版本，无需更新时抛出[NoModUpdatesAvailableException]
 */
class NoModUpdatesAvailableException : RuntimeException("All mods are already up to date")