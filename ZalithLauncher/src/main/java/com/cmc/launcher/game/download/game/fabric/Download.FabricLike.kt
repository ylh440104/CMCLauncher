 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.game.fabric

import com.cmc.launcher.coroutine.Task
import com.cmc.launcher.game.addons.mirror.mapBMCLMirrorUrls
import com.cmc.launcher.game.addons.modloader.fabriclike.FabricLikeVersion
import com.cmc.launcher.utils.file.ensureParentDirectory
import com.cmc.launcher.utils.network.fetchStringFromUrls
import kotlinx.coroutines.Dispatchers
import java.io.File

const val FABRIC_LIKE_DOWNLOAD_ID = "Download.FabricLike"

fun getFabricLikeDownloadTask(
    fabricLikeVersion: FabricLikeVersion,
    tempVersionJson: File
): Task {
    return Task.runTask(
        id = FABRIC_LIKE_DOWNLOAD_ID,
        dispatcher = Dispatchers.IO,
        task = {
            //下载版本 Json
            val loaderJson = fetchStringFromUrls(fabricLikeVersion.loaderJsonUrl.mapBMCLMirrorUrls())
            tempVersionJson
                .ensureParentDirectory()
                .writeText(loaderJson)
        }
    )
}