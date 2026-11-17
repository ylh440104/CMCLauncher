 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.setting.enums

/**
 * 背景内容模糊效果类型
 */
enum class BackgroundBlur {
    /**
     * 背景模糊
     */
    Background,
    /**
     * 前景毛玻璃模糊
     */
    Foreground;

    fun switch(): BackgroundBlur {
        return when (this) {
            Background -> Foreground
            Foreground -> Background
        }
    }
}