 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.utils.video

import android.media.MediaMetadataRetriever
import com.cmc.launcher.utils.logging.Logger
import java.io.File

private const val TAG = "VideoUtils"

/**
 * 尝试判断文件是否为一则视频
 */
fun File.isVideoFile(): Boolean {
    if (!exists()) return false
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(absolutePath)
        val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) != null &&
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) != null
        hasVideo
    } catch (e: Exception) {
        Logger.warning(TAG, "An exception occurred while trying to determine if $absolutePath is a video.", e)
        false
    } finally {
        retriever.release()
    }
}