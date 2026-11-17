 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.setting.enums

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.cmc.launcher.R

enum class AppLanguage(
    val tag: String,
    @param:StringRes val textRes: Int
) {
    //按语言代码字母顺序排序
    FOLLOW_SYSTEM("", R.string.generic_follow_system),
    ARABIC("ar", R.string.language_arabic),
    ENGLISH("en", R.string.language_english),
    SPANISH("es", R.string.language_spanish),
    FILIPINO("fil", R.string.language_filipino),
    INDONESIAN("id", R.string.language_indonesian),
    ITALIAN("it", R.string.language_italian),
    JAPANESE("ja", R.string.language_japanese),
    KOREAN("ko", R.string.language_korean),
    PORTUGUESE("pt", R.string.language_portuguese),
    BRAZILIAN_PORTUGUESE("pt-BR", R.string.language_brazilian_portuguese),
    RUSSIAN("ru", R.string.language_russian),
    THAI("th", R.string.language_thai),
    TURKISH("tr", R.string.language_turkish),
    UYGHUR("ug", R.string.language_uyghur),
    VIETNAMESE("vi", R.string.language_vietnamese),
    SIMPLIFIED_CHINESE("zh-CN", R.string.language_simplified_chinese),
    TRADITIONAL_CHINESE("zh-TW", R.string.language_traditional_chinese),
}

fun applyLanguage(language: AppLanguage) {
    val appLocale = if (language != AppLanguage.FOLLOW_SYSTEM) {
        LocaleListCompat.forLanguageTags(language.tag)
    } else {
        LocaleListCompat.getEmptyLocaleList()
    }
    AppCompatDelegate.setApplicationLocales(appLocale)
}
