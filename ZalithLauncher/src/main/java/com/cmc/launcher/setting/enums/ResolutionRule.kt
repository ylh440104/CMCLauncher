 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.setting.enums

import com.cmc.launcher.R

/**
 * 游戏分辨率的计算规则
 */
enum class ResolutionRule(val nameRes: Int) {
    /**
     * 基于屏幕真实宽高按百分比缩放
     */
    PERCENTAGE(R.string.settings_renderer_resolution_rule_percentage),
    /**
     * 精确指定渲染宽高
     */
    CUSTOM(R.string.generic_custom)
}
