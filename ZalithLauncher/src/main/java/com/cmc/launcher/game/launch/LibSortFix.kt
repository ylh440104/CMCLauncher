 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.launch

import com.cmc.launcher.game.versioninfo.models.GameManifest

/**
 * 依赖库加载顺序修复 & 依赖库检查
 * 为以后方便扩展考虑诞生的东西（
 */
class LibSortFix {
    /**
     * 检查并插入依赖库
     */
    fun LinkedHashMap<GameManifest.Library, String>.insertLib(
        libItem: GameManifest.Library,
        path: String
    ) {
        val name = libItem.name
        if (name.startsWith("org.ow2.asm:asm:")) {
            removeIf { (key, _) ->
                key.name.startsWith("org.ow2.asm:asm-all:")
            }
        }

        this[libItem] = path
    }

    private fun <K, V> LinkedHashMap<K, V>.removeIf(predicate: (Map.Entry<K, V>) -> Boolean) {
        val iterator = this.entries.iterator()
        while (iterator.hasNext()) {
            if (predicate(iterator.next())) {
                iterator.remove()
            }
        }
    }
}
