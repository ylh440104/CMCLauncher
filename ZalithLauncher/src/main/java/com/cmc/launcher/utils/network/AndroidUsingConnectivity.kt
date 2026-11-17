 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.utils.network

import android.net.ConnectivityManager
import com.cmc.launcher.context.GlobalContext

/**
 * 获取设备当前网络的真实 DNS 服务器
 */
fun getSystemDnsServerAddresses(): List<String>? {
    return runCatching {
        val connectivityManager = GlobalContext.applicationContext
            .getSystemService(ConnectivityManager::class.java) ?: return null
        val network = connectivityManager.activeNetwork ?: return null
        val linkProperties = connectivityManager.getLinkProperties(network) ?: return null

        linkProperties.dnsServers
            .mapNotNull { it.hostAddress?.takeIf(String::isNotEmpty) }
            .takeIf(List<String>::isNotEmpty)
    }.getOrNull()
}
