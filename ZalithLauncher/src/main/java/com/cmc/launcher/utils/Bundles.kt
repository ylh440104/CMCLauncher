 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.utils

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

@Suppress("DEPRECATION")
fun <T : Parcelable> Bundle.getParcelableSafely(key: String?, clazz: Class<T>): T? {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelable(key, clazz)
        else -> getParcelable(key)
    }
}

@Suppress("DEPRECATION")
fun <T: Serializable> Bundle.getSerializableSafely(key: String?, clazz: Class<T>): T? {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializable(key, clazz)
        else -> getSerializable(key) as? T
    }
}