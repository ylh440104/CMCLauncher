 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui.screens.content

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import com.cmc.launcher.R
import com.cmc.launcher.ui.base.BaseScreen
import com.cmc.launcher.ui.components.AnimatedDirection
import com.cmc.launcher.ui.components.animated
import com.cmc.launcher.ui.components.fadeEdge
import com.cmc.launcher.ui.components.rememberAnimatedContext
import com.cmc.launcher.ui.screens.NestedNavDisplay
import com.cmc.launcher.ui.screens.NestedNavKey
import com.cmc.launcher.ui.screens.NormalNavKey
import com.cmc.launcher.ui.screens.TitledNavKey
import com.cmc.launcher.ui.screens.content.elements.CategoryIcon
import com.cmc.launcher.ui.screens.content.elements.CategoryItem
import com.cmc.launcher.ui.screens.content.settings.JavaManageScreen
import com.cmc.launcher.ui.screens.content.settings.MirrorSettingScreen
import com.cmc.launcher.ui.screens.navigateOnce
import com.cmc.launcher.ui.screens.onBack
import com.cmc.launcher.ui.screens.rememberTransitionSpec
import com.cmc.launcher.viewmodel.ErrorViewModel
import com.cmc.launcher.viewmodel.EventViewModel
import com.cmc.launcher.viewmodel.ScreenBackStackViewModel

@Composable
fun SettingsScreen(
    key: NestedNavKey.Settings,
    backStackViewModel: ScreenBackStackViewModel,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    BaseScreen(
        screenKey = key,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->

        Column(modifier = Modifier.fillMaxSize()) {
            TabMenu(
                modifier = Modifier.fillMaxWidth(),
                isVisible = isVisible,
                settingsScreenKey = backStackViewModel.settingsScreen.currentKey,
                navigateTo = { settingKey ->
                    key.backStack.navigateOnce(settingKey)
                }
            )
            NavigationUI(
                key = key,
                mainScreenKey = backStackViewModel.mainScreen.currentKey,
                settingsScreenKey = backStackViewModel.settingsScreen.currentKey,
                onCurrentKeyChange = { newKey ->
                    backStackViewModel.settingsScreen.currentKey = newKey
                },
                eventViewModel = eventViewModel,
                submitError = submitError,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        }
    }
}

private val settingItems = listOf(
    CategoryItem(NormalNavKey.Settings.JavaManager, { CategoryIcon(R.drawable.ic_java, R.string.settings_tab_java_manage) }, R.string.settings_tab_java_manage),
    CategoryItem(NormalNavKey.Settings.Mirror, { CategoryIcon(R.drawable.ic_public, R.string.generic_setting) }, R.string.generic_setting)
)

@Composable
private fun TabMenu(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    settingsScreenKey: TitledNavKey?,
    navigateTo: (TitledNavKey) -> Unit
) {
    val ctx = rememberAnimatedContext(isVisible = isVisible)
    val selected = settingItems.indexOfFirst { it.key == settingsScreenKey }.coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animated(ctx, from = AnimatedDirection.Top)
    ) {
        PrimaryTabRow(
            selectedTabIndex = selected,
            modifier = Modifier.fillMaxWidth()
        ) {
            settingItems.forEachIndexed { index, item ->
                Tab(
                    selected = index == selected,
                    onClick = { navigateTo(item.key) },
                    text = {
                        Text(
                            text = stringResource(item.textRes),
                            maxLines = 1,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    icon = {
                        item.icon()
                    }
                )
            }
        }
    }
}

@Composable
private fun NavigationUI(
    key: NestedNavKey.Settings,
    mainScreenKey: TitledNavKey?,
    settingsScreenKey: TitledNavKey?,
    onCurrentKeyChange: (TitledNavKey?) -> Unit,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = key.backStack
    val stackTopKey = backStack.lastOrNull()
    LaunchedEffect(stackTopKey) {
        onCurrentKeyChange(stackTopKey)
    }

    if (backStack.isNotEmpty()) {
        NestedNavDisplay(
            ownerKey = key,
            backStack = backStack,
            modifier = modifier,
            onBack = {
                onBack(backStack)
            },
            transitionSpec = rememberTransitionSpec(),
            popTransitionSpec = rememberTransitionSpec(),
            entryProvider = entryProvider {
                entry<NormalNavKey.Settings.JavaManager> {
                    JavaManageScreen(key, settingsScreenKey, mainScreenKey, eventViewModel, submitError)
                }
                entry<NormalNavKey.Settings.Mirror> {
                    MirrorSettingScreen(
                        key = key,
                        settingsScreenKey = settingsScreenKey,
                        mainScreenKey = mainScreenKey
                    )
                }
            }
        )
    } else {
        Box(modifier)
    }
}