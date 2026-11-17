 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.setting.unit

import com.cmc.launcher.setting.launcherMMKV

class IntSettingUnit(
    key: String,
    defaultValue: Int,
    val valueRange: IntRange,
) : AbstractSettingUnit<Int>(key, defaultValue) {
    override fun getValue(): Int {
        return launcherMMKV().getInt(key, defaultValue)
            .coerceIn(valueRange)
            .also { state = it }
    }

    override fun saveValue(v: Int): Int {
        return v.coerceIn(valueRange).also { value ->
            launcherMMKV().putInt(key, value).apply()
        }
    }

    override fun updateState(value: Int) {
        this.state = value.coerceIn(valueRange)
    }
}