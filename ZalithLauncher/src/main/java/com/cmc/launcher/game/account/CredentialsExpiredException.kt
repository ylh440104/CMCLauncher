 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.account

/**
 * 本地存储的登录凭据已被服务端拒绝
 * 无法通过刷新恢复，必须重新登录账号
 */
class CredentialsExpiredException : RuntimeException("Login credentials have expired")
