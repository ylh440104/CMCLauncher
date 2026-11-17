 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.renderer.renderers

import com.cmc.launcher.game.renderer.RendererInterface
import com.cmc.launcher.path.PathManager
import java.io.File

object VirGLRenderer : RendererInterface {
    override fun getRendererId(): String = "gallium_virgl"

    override fun getUniqueIdentifier(): String = "a3ccc1fe-de3f-4a81-8c45-2485181b63b3"

    override val mesaBased: Boolean = true

    override fun getRendererName(): String = "VirGLRenderer"

    override fun getMaxMCVersion(): String = "26.3-snapshot-3"

    override fun getDisplayMaxMCVersion(): String = "26.2"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        mapOf(
            "VTEST_SOCKET_NAME" to File(PathManager.DIR_CACHE, ".virgl_test").absolutePath
        )
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libOSMesa_2121.so"
}