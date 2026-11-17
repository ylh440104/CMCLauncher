 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.setting.unit

val DEFAULT_INT_RANGE: IntRange = Int.MIN_VALUE..Int.MAX_VALUE

val DEFAULT_FLOAT_RANGE: ClosedFloatingPointRange<Float> = Float.MIN_VALUE..Float.MAX_VALUE

val DEFAULT_LONG_RANGE: LongRange = Long.MIN_VALUE..Long.MAX_VALUE

fun IntRange.toFloatRange(): ClosedFloatingPointRange<Float> = start.toFloat()..endInclusive.toFloat()

val IntSettingUnit.floatRange: ClosedFloatingPointRange<Float>
    get() = valueRange.toFloatRange()

val NullableIntSettingUnit.floatRange: ClosedFloatingPointRange<Float>
    get() = valueRange.toFloatRange()