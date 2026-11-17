 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.addon.platform

import com.cmc.launcher.ui.screens.content.download.addon.elements.AddonPage

fun previousPage(
    pageNumber: Int,
    pages: List<AddonPage?>,
    index: Int,
    limit: Int,
    onSuccess: (AddonPage) -> Unit = {},
    onSearch: (index: Int) -> Unit = {}
) {
    val targetIndex = pageNumber - 2 //上一页在缓存中的索引
    val previousPage = pages.getOrNull(targetIndex)
    if (previousPage != null) {
        onSuccess(previousPage)
    } else {
        //重新搜索
        onSearch((index - limit).coerceAtLeast(0))
    }
}

fun nextPage(
    pageNumber: Int,
    isLastPage: Boolean,
    pages: List<AddonPage?>,
    index: Int,
    limit: Int,
    onSuccess: (AddonPage) -> Unit = {},
    onSearch: (index: Int) -> Unit = {}
) {
    if (!isLastPage) {
        val nextIndex = pageNumber
        //判断是否已缓存下一页
        val nextPage = pages.getOrNull(nextIndex)
        if (nextPage != null) {
            onSuccess(nextPage)
        } else {
            //搜索下一页
            onSearch(index + limit)
        }
    }
}

fun navigatePage(
    pageNumber: Int,
    pages: List<AddonPage?>,
    limit: Int,
    onSuccess: (AddonPage) -> Unit = {},
    onSearch: (index: Int) -> Unit = {}
) {
    val targetNumber = pageNumber - 1
    //判断是否已缓存目标页
    val targetPage = pages.getOrNull(targetNumber)
    if (targetPage != null) {
        onSuccess(targetPage)
    } else {
        //搜索目标页
        onSearch(targetNumber * limit)
    }
}