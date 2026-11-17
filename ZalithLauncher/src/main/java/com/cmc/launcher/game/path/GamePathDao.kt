 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.path

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GamePathDao {
    @Query("SELECT * FROM gamePaths")
    suspend fun getAllPaths(): List<GamePath>

    @Query("SELECT * FROM gamePaths WHERE id = :id")
    suspend fun getPath(id: String): GamePath?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePath(path: GamePath)

    @Delete
    suspend fun deletePath(path: GamePath)
}