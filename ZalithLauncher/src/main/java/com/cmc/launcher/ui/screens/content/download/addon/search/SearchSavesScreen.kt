 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui.screens.content.download.addon.search

import androidx.compose.runtime.Composable
import com.cmc.launcher.game.download.addon.platform.Platform
import com.cmc.launcher.game.download.addon.platform.PlatformClasses
import com.cmc.launcher.game.download.addon.platform.curseforge.models.CurseForgeSavesCategory
import com.cmc.launcher.ui.screens.NormalNavKey
import com.cmc.launcher.ui.screens.TitledNavKey

@Composable
fun SearchSavesScreen(
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadSavesScreenKey: TitledNavKey,
    downloadSavesScreenCurrentKey: TitledNavKey?,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> }
) {
    SearchAddonScreen(
        mainScreenKey = mainScreenKey,
        parentScreenKey = downloadSavesScreenKey,
        parentCurrentKey = downloadScreenKey,
        screenKey = NormalNavKey.SearchSaves,
        currentKey = downloadSavesScreenCurrentKey,
        platformClasses = PlatformClasses.SAVES,
        initialPlatform = Platform.CURSEFORGE,
        enablePlatform = false,
        getCategories = { CurseForgeSavesCategory.entries },
        mapCategories = { platform, string ->
            CurseForgeSavesCategory.entries.find { it.describe() == string }
        },
        swapToDownload = swapToDownload
    )
}