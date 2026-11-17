 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.path

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 游戏目录
 */
@Entity(tableName = "gamePaths")
data class GamePath(
    /**
     * 单项唯一ID
     */
    @PrimaryKey
    val id: String,
    /**
     * 游戏目录的标题
     */
    var title: String,
    /**
     * 目标路径
     */
    val path: String
)
