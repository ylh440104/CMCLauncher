 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.setting.unit

import com.cmc.launcher.setting.launcherMMKV

class FloatSettingUnit(
    key: String,
    defaultValue: Float,
    val valueRange: ClosedFloatingPointRange<Float>
) : AbstractSettingUnit<Float>(key, defaultValue) {
    override fun getValue(): Float {
        return launcherMMKV().getFloat(key, defaultValue)
            .also { state = it }
    }

    override fun saveValue(v: Float): Float {
        return v.coerceIn(valueRange).also { value ->
            launcherMMKV().putFloat(key, value).apply()
        }
    }

    override fun updateState(value: Float) {
        this.state = value.coerceIn(valueRange)
    }
}