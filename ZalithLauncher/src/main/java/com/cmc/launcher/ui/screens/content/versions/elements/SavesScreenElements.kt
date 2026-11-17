 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui.screens.content.versions.elements

import com.cmc.launcher.game.version.saves.SaveData
import com.cmc.launcher.game.version.saves.isCompatible
import com.cmc.launcher.utils.string.stripColorCodes

sealed interface SavesOperation {
    data object None : SavesOperation
    /** 执行任务中 */
    data object Progress : SavesOperation
    /** 快速启动 */
    data class QuickPlay(val saveData: SaveData) : SavesOperation
    /** 重命名存档输入对话框 */
    data class RenameSave(val saveData: SaveData) : SavesOperation
    /** 备份存档输入对话框 */
    data class BackupSave(val saveData: SaveData) : SavesOperation
    /** 删除存档对话框 */
    data class DeleteSave(val saveData: SaveData) : SavesOperation
}

/**
 * 存档压缩分享任务的操作状态
 */
sealed interface SavesCompressOperation {
    data object None : SavesCompressOperation
    /** 正在压缩存档 */
    data class Compressing(val saveData: SaveData) : SavesCompressOperation
    /** 压缩过程出现异常 */
    data class Error(val throwable: Throwable) : SavesCompressOperation
}

/**
 * 存档过滤器
 */
data class SavesFilter(val onlyShowCompatible: Boolean, val saveName: String = "")

/**
 * 简易过滤器，过滤特定的存档
 * @param minecraftVersion 当前 MC 的版本，用于比较版本兼容性
 * @param savesFilter 存档过滤器
 */
fun List<SaveData>.filterSaves(
    minecraftVersion: String,
    savesFilter: SavesFilter
) = this.filter {
    val isCompatible = !savesFilter.onlyShowCompatible || it.isCompatible(minecraftVersion)

    val nameMatches = savesFilter.saveName.isEmpty() ||
            //存档名、存档文件夹名均可参与搜索
            //自动过滤掉颜色占位符
            it.levelName?.stripColorCodes()?.contains(savesFilter.saveName, true) == true ||
            it.saveFile.name.stripColorCodes().contains(savesFilter.saveName, true)

    isCompatible && nameMatches
}