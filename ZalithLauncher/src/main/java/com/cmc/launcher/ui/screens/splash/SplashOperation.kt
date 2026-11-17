 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui.screens.splash

import com.cmc.launcher.ui.screens.TitledNavKey
import kotlinx.serialization.Serializable

sealed interface SplashOperation : TitledNavKey {
    @Serializable data object Loading : SplashOperation
    @Serializable data object Welcome : SplashOperation
    @Serializable data object Unpacking : SplashOperation
}
