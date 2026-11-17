 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.components

import com.cmc.launcher.R

enum class Components(
    val component: String,
    val displayName: String,
    val summary: Int,
    val assetsDir: String
) {
    AUTH_LIBS(
        "auth_libs", "authlib-injector", R.string.unpack_screen_authlib_injector,
        assetsDir = "components/auth_libs"
    ),
    CACIOCAVALLO(
        "caciocavallo", "caciocavallo", R.string.unpack_screen_cacio,
        assetsDir = "components/caciocavallo"
    ),
    CACIOCAVALLO17(
        "caciocavallo17", "caciocavallo 17", R.string.unpack_screen_cacio,
        assetsDir = "components/caciocavallo17"
    ),
    LWJGL333(
        "lwjgl/3.3.3", "LWJGL 3.3.3", R.string.unpack_screen_lwjgl,
        assetsDir = "app_runtime/lwjgl/3.3.3"
    ),
    LWJGL341(
        "lwjgl/3.4.1", "LWJGL 3.4.1", R.string.unpack_screen_lwjgl,
        assetsDir = "app_runtime/lwjgl/3.4.1"
    ),
    LAUNCHER(
        "launcher", "Launcher Components", R.string.unpack_screen_launcher,
        assetsDir = "components/launcher"
    )
}