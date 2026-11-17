 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.setting

import com.cmc.launcher.BuildKeys
import com.tencent.mmkv.MMKV

/**
 * 启动器全局 MMKV，管理所有设置项
 */
fun launcherMMKV(): MMKV = MMKV.mmkvWithID(BuildKeys.LAUNCHER_IDENTIFIER, MMKV.MULTI_PROCESS_MODE)