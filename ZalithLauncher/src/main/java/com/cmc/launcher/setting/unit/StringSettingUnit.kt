 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.setting.unit

import com.cmc.launcher.setting.launcherMMKV

class StringSettingUnit(key: String, defaultValue: String) : AbstractSettingUnit<String>(key, defaultValue) {
    override fun getValue(): String {
        return launcherMMKV().getString(key ,defaultValue)!!
            .also { state = it }
    }

    override fun saveValue(v: String): String {
        launcherMMKV().putString(key, v).apply()
        return v
    }
}