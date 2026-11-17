 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.engine

/**
 * 单文件下载作业：把 [DownloadRequest] 交给 [Fetcher] 按候选源顺序下载，
 * 期间把落盘字节计入 [stats]。
 */
class FileDownloader(
    private val request: DownloadRequest,
    private val stats: DownloadStats
) {
    suspend fun download() {
        Fetcher.downloadFile(
            urls = request.urls,
            targetFile = request.targetFile,
            sha1 = request.sha1,
            onBytes = stats::addBytes
        )
        //下载前未知大小的文件：分子已随下载计入，这里把实际大小补进分母
        if (request.expectedSize <= 0) {
            stats.registerBytes(request.targetFile.length())
        }
    }
}
