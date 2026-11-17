 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.viewmodel

import androidx.lifecycle.ViewModel
import com.cmc.launcher.ui.screens.NestedNavKey

class SplashBackStackViewModel: ViewModel() {
    /** 启动屏幕 */
    val splashScreen = NestedNavKey.Splash()
}