 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.setting.unit

import com.cmc.launcher.setting.launcherMMKV

class BooleanSettingUnit(key: String, defaultValue: Boolean) : AbstractSettingUnit<Boolean>(key, defaultValue) {
    override fun getValue(): Boolean {
        return launcherMMKV().getBoolean(key, defaultValue)
            .also { state = it }
    }

    override fun saveValue(v: Boolean): Boolean {
        launcherMMKV().putBoolean(key, v).apply()
        return v
    }
}