 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.addons.modloader.forgelike.neoforge.models

import com.cmc.launcher.game.addons.modloader.forgelike.neoforge.NeoForgeVersion

interface NeoForgeMergeableMaven<E>  {
    /**
     * 将自己的版本数据与其他的版本数据进行合并
     */
    operator fun plus(maven: E): List<NeoForgeVersion>

    fun isVersionInvalid(versionId: String): Boolean {
        val cantDownload = versionId == "47.1.82" //这个版本虽然在版本列表中，但不能下载
        val isAlpha = versionId.contains("-alpha") //Alpha版本不太稳定，避免下载
        return cantDownload || isAlpha
    }
}