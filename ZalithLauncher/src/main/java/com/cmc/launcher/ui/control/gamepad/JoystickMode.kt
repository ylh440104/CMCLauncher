 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui.control.gamepad

import com.cmc.launcher.R

/**
 * 摇杆控制模式
 */
enum class JoystickMode(val titleRes: Int, val summaryRes: Int) {
    /**
     * 使用左摇杆控制移动，右摇杆控制视角
     */
    LeftMovement(
        titleRes = R.string.settings_gamepad_joystick_mode_left,
        summaryRes = R.string.settings_gamepad_joystick_mode_left_summary
    ),

    /**
     * 使用右摇杆控制移动，左摇杆控制视角
     */
    RightMovement(
        titleRes = R.string.settings_gamepad_joystick_mode_right,
        summaryRes = R.string.settings_gamepad_joystick_mode_right_summary
    )
}