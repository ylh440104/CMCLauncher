 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.setting.unit

import androidx.compose.ui.geometry.Offset
import com.cmc.launcher.setting.launcherMMKV

class OffsetSettingUnit(
    key: String,
    defaultValue: Offset
) : AbstractSettingUnit<Offset>(key, defaultValue) {
    override fun getValue(): Offset {
        val long = launcherMMKV().getLong(key, defaultValue.packedValue)
        return Offset(long).also { state = it }
    }

    override fun saveValue(v: Offset): Offset {
        launcherMMKV().putLong(key, v.packedValue).apply()
        return v
    }
}