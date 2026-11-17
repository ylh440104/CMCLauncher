 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.setting.unit

val IntSettingUnit.min: Int
    get() = valueRange.first

val IntSettingUnit.max: Int
    get() = valueRange.last

val NullableIntSettingUnit.min: Int
    get() = valueRange.first

val NullableIntSettingUnit.max: Int
    get() = valueRange.last

fun NullableIntSettingUnit.getOrMin(): Int = getValue() ?: min

fun NullableIntSettingUnit.getOrMax(): Int = getValue() ?: max

val FloatSettingUnit.min: Float
    get() = valueRange.start

val FloatSettingUnit.max: Float
    get() = valueRange.endInclusive

val LongSettingUnit.min: Long
    get() = valueRange.first

val LongSettingUnit.max: Long
    get() = valueRange.last