 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.account.wardrobe

import java.io.File

class CapeFileDownloader: WardrobeDownloader() {
    /**
     * 尝试下载yggdrasil皮肤
     */
    @Throws(Exception::class)
    suspend fun download(
        url: String,
        capeFile: File,
        uuid: String,
    ) {
        val valueObject = yggdrasil(url, uuid)
        val capeObject = valueObject.get("textures").asJsonObject.get("CAPE").asJsonObject
        val skinUrl = capeObject.get("url").asString

        download(skinUrl, capeFile)
    }
}