 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.game.fabric

import com.cmc.launcher.coroutine.Task
import com.cmc.launcher.game.download.game.GameLibDownloader
import com.cmc.launcher.game.version.download.BaseMinecraftDownloader
import java.io.File

const val FABRIC_LIKE_COMPLETER_ID = "Completer.FabricLike"

fun getFabricLikeCompleterTask(
    downloader: BaseMinecraftDownloader,
    tempMinecraftDir: File,
    tempVersionJson: File,
): Task {
    return Task.runTask(
        id = FABRIC_LIKE_COMPLETER_ID,
        task = { task ->
            val libDownloader = GameLibDownloader(
                downloader = downloader,
                gameJson = tempVersionJson.readText()
            )
            //提交下载计划
            libDownloader.schedule(task, File(tempMinecraftDir, "libraries"))
            //补全游戏库
            libDownloader.download(task)
        }
    )
}