 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui.control.joystick

import com.movtery.inputmap.keycodes.MOVEMENT_BACK
import com.movtery.inputmap.keycodes.MOVEMENT_BACK_VALUE
import com.movtery.inputmap.keycodes.MOVEMENT_FORWARD
import com.movtery.inputmap.keycodes.MOVEMENT_FORWARD_VALUE
import com.movtery.inputmap.keycodes.MOVEMENT_LEFT
import com.movtery.inputmap.keycodes.MOVEMENT_LEFT_VALUE
import com.movtery.inputmap.keycodes.MOVEMENT_RIGHT
import com.movtery.inputmap.keycodes.MOVEMENT_RIGHT_VALUE

/**
 * 摇杆的每个方向代表的移动键键值
 */
val directionMapping = mapOf(
    JoystickDirection.East to listOf(
        MOVEMENT_RIGHT to MOVEMENT_RIGHT_VALUE
    ),
    JoystickDirection.NorthEast to listOf(
        MOVEMENT_FORWARD to MOVEMENT_FORWARD_VALUE,
        MOVEMENT_RIGHT to MOVEMENT_RIGHT_VALUE
    ),
    JoystickDirection.North to listOf(
        MOVEMENT_FORWARD to MOVEMENT_FORWARD_VALUE
    ),
    JoystickDirection.NorthWest to listOf(
        MOVEMENT_FORWARD to MOVEMENT_FORWARD_VALUE,
        MOVEMENT_LEFT to MOVEMENT_LEFT_VALUE
    ),
    JoystickDirection.West to listOf(
        MOVEMENT_LEFT to MOVEMENT_LEFT_VALUE
    ),
    JoystickDirection.SouthWest to listOf(
        MOVEMENT_BACK to MOVEMENT_BACK_VALUE,
        MOVEMENT_LEFT to MOVEMENT_LEFT_VALUE
    ),
    JoystickDirection.South to listOf(
        MOVEMENT_BACK to MOVEMENT_BACK_VALUE
    ),
    JoystickDirection.SouthEast to listOf(
        MOVEMENT_BACK to MOVEMENT_BACK_VALUE,
        MOVEMENT_RIGHT to MOVEMENT_RIGHT_VALUE
    ),
    JoystickDirection.None to emptyList()
)

/**
 * 所有的移动键键值
 */
val allAction = listOf(
    MOVEMENT_FORWARD to MOVEMENT_FORWARD_VALUE,
    MOVEMENT_BACK to MOVEMENT_BACK_VALUE,
    MOVEMENT_LEFT to MOVEMENT_LEFT_VALUE,
    MOVEMENT_RIGHT to MOVEMENT_RIGHT_VALUE
)
