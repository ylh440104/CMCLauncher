 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.components

import android.content.Context
import com.cmc.launcher.path.PathManager

class UnpackComponentsTask(context: Context, val component: Components) : UnpackSingleTask(
    context = context,
    rootDir = PathManager.DIR_COMPONENTS,
    assetsDirName = component.assetsDir,
    fileDirName = component.component
)