 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.setting.unit

import android.os.Parcelable
import com.cmc.launcher.setting.launcherMMKV
import kotlinx.parcelize.Parcelize

class StringListSettingUnit(
    key: String,
    defaultValue: List<String>
) : AbstractSettingUnit<List<String>>(
    key = key,
    defaultValue = defaultValue
) {
    override fun getValue(): List<String> {
        val list = launcherMMKV().decodeParcelable(key, ListParcelable::class.java)
        return (list?.value ?: defaultValue)
            .also { state = it }
    }

    override fun saveValue(v: List<String>): List<String> {
        launcherMMKV().encode(key, ListParcelable(v))
        return v
    }
}

@Parcelize
private data class ListParcelable(val value: List<String>) : Parcelable