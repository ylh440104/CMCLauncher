 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.account.wardrobe

enum class SkinType {
    /** 尺寸不合法 */
    Invalid,
    /** 双层皮肤 */
    DualLayer,
    /** 单层皮肤（旧版） */
    Classic
}

/**
 * @return 当前皮肤是否合法
 */
fun SkinType.isValid(): Boolean = this != SkinType.Invalid