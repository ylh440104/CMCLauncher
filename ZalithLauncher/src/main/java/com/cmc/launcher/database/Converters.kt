 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.database

import androidx.room.TypeConverter
import com.cmc.launcher.game.account.wardrobe.SkinModelType

class Converters {
    @TypeConverter
    fun fromSkinModelType(type: SkinModelType): String = type.name

    @TypeConverter
    fun toSkinModelType(value: String): SkinModelType =
        enumValueOf(value)
}