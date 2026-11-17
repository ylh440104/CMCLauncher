 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.modpack.install

/**
 * 用户拒绝使用移动网络安装整合包
 */
class UsingMobileDataException: RuntimeException(
    "Users do not want to install the modpack using mobile data."
)