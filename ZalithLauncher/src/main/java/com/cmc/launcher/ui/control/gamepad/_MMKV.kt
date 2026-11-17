 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui.control.gamepad

import com.tencent.mmkv.MMKV

/**
 * 手柄重映射数据保存 MMKV
 */
fun remapperMMKV(): MMKV = MMKV.mmkvWithID("GamepadRemapper", MMKV.MULTI_PROCESS_MODE)

/**
 * 手柄键值绑定数据保存 MMKV
 */
fun keyMappingMMKV(): MMKV = MMKV.mmkvWithID("GamepadKeyMapping", MMKV.MULTI_PROCESS_MODE)

/**
 * 手柄按键映射配置列表保存 MMKV
 */
fun keyMappingListMMKV(): MMKV = MMKV.mmkvWithID("GamepadKeyMappingList", MMKV.MULTI_PROCESS_MODE)


