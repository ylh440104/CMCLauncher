 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui.control.input

/**
 * 当前输入模式
 */
enum class TextInputMode {
    /**
     * 启用
     */
    ENABLE {
        override fun switch() = DISABLE
    },

    /**
     * 禁用
     */
    DISABLE {
        override fun switch() = ENABLE
    };

    abstract fun switch(): TextInputMode
}