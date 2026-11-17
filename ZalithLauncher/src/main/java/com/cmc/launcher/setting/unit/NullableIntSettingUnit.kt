 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.setting.unit

import com.cmc.launcher.setting.launcherMMKV

class NullableIntSettingUnit(
    key: String,
    defaultValue: Int?,
    val valueRange: IntRange
) : AbstractSettingUnit<Int?>(key, defaultValue) {
    override fun getValue(): Int? {
        val mmkv = launcherMMKV()
        return if (mmkv.containsKey(key)) {
            mmkv.getInt(key, 0)
        } else {
            defaultValue
        }.also { state = it }
    }

    override fun saveValue(v: Int?): Int? {
        val mmkv = launcherMMKV()
        return if (v == null) {
            mmkv.remove(key).apply()
            null
        } else {
            v.coerceIn(valueRange).also { value ->
                mmkv.putInt(key, value).apply()
            }
        }
    }

    override fun updateState(value: Int?) {
        this.state = value?.coerceIn(valueRange)
    }
}