 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * 获取不受应用内语言设置影响的系统语言列表
 */
private fun getSystemLocaleList(context: Context): LocaleList {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.getSystemService(LocaleManager::class.java)?.systemLocales?.let { return it }
    }
    return Resources.getSystem().configuration.locales
}

/**
 * 按系统语言解析字符串资源，不随应用内语言设置变化
 */
@Composable
fun systemLocaleStringResource(@StringRes id: Int): String {
    val context = LocalContext.current
    val systemLocales = remember { getSystemLocaleList(context) }
    return remember(id, systemLocales) {
        val configuration = Configuration()
        configuration.setLocales(systemLocales)
        context.createConfigurationContext(configuration).getString(id)
    }
}
