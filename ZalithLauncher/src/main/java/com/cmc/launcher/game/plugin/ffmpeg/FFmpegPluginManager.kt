 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.plugin.ffmpeg

import android.content.Context
import android.content.pm.PackageManager
import com.cmc.launcher.game.plugin.ApkPlugin
import com.cmc.launcher.game.plugin.cacheAppIcon
import com.cmc.launcher.utils.logging.Logger
import java.io.File

private const val TAG = "FFmpegPlugin"

object FFmpegPluginManager {
    private const val PLUGIN_PACKAGE_NAME = "net.kdt.pojavlaunch.ffmpeg"

    var libraryPath: String? = null
        private set

    var executablePath: String? = null
        private set

    /**
     * 插件是否可用
     */
    var isAvailable: Boolean = false
        private set

    /**
     * 加载 FFmpeg 插件
     */
    fun loadPlugin(
        context: Context,
        loaded: (ApkPlugin) -> Unit = {}
    ) {
        val manager: PackageManager = context.packageManager
        runCatching {
            val info = try {
                manager.getPackageInfo(
                    PLUGIN_PACKAGE_NAME,
                    PackageManager.GET_SHARED_LIBRARY_FILES
                )
            } catch (_: PackageManager.NameNotFoundException) {
                //未安装
                return
            }
            val applicationInfo = info.applicationInfo!!
            libraryPath = applicationInfo.nativeLibraryDir
            val ffmpegExecutable = File(libraryPath, "libffmpeg.so")
            executablePath = ffmpegExecutable.absolutePath
            isAvailable = ffmpegExecutable.exists()

            if (isAvailable) {
                cacheAppIcon(context, applicationInfo)
                runCatching {
                    ApkPlugin(
                        packageName = PLUGIN_PACKAGE_NAME,
                        appName = applicationInfo.loadLabel(manager).toString(),
                        appVersion = manager.getPackageInfo(PLUGIN_PACKAGE_NAME, 0).versionName ?: ""
                    )
                }.getOrNull()?.let { loaded(it) }
            }
        }.onFailure { e ->
            Logger.warning(TAG, "Failed to discover plugin", e)
        }
    }
}