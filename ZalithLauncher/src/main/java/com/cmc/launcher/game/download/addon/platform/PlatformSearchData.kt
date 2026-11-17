 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.addon.platform

/**
 * 平台的搜索结果单项数据实现
 */
interface PlatformSearchData {
    /**
     * 所属平台
     */
    fun platform(): Platform

    /**
     * 项目Id
     */
    fun platformId(): String

    /**
     * 在平台上的标题
     */
    fun platformTitle(): String

    /**
     * 在平台上的描述
     */
    fun platformDescription(): String

    /**
     * 在平台上的作者列表
     */
    fun platformAuthors(): List<String>?

    /**
     * 图标链接
     */
    fun platformIconUrl(): String?

    /**
     * 在平台上的下载数量
     */
    fun platformDownloadCount(): Long

    /**
     * 在平台上的收藏数量（Modrinth）
     */
    fun platformFollows(): Long?

    /**
     * 在平台上标注的模组加载器信息
     */
    fun platformModLoaders(): List<PlatformDisplayLabel>?

    /**
     * 在平台上标注的类别信息
     */
    fun platformCategories(classes: PlatformClasses): List<PlatformFilterCode>?
}