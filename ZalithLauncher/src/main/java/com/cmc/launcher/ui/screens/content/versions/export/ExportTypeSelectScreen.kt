package com.cmc.launcher.ui.screens.content.versions.export

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cmc.launcher.BuildKeys
import com.cmc.launcher.R
import com.cmc.launcher.game.version.export.PackType
import com.cmc.launcher.ui.base.BaseScreen
import com.cmc.launcher.ui.components.AnimatedLazyColumn
import com.cmc.launcher.ui.components.BackgroundCard
import com.cmc.launcher.ui.components.WarningCard
import com.cmc.launcher.ui.components.animated
import com.cmc.launcher.ui.screens.NestedNavKey
import com.cmc.launcher.ui.screens.NormalNavKey
import com.cmc.launcher.ui.screens.TitledNavKey
import com.cmc.launcher.ui.screens.content.settings.layouts.CardPosition
import com.cmc.launcher.ui.screens.content.settings.layouts.rememberSettingsCardShape

/**
 * 整合包导出类型选择页
 */
@Composable
fun ExportTypeSelectScreen(
    mainScreenKey: TitledNavKey?,
    exportScreenKey: TitledNavKey?,
    onTypeSelect: (PackType) -> Unit
) {
    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.VersionExport::class.java, mainScreenKey)
        ),
        Triple(NormalNavKey.VersionExports.SelectType, exportScreenKey, false)
    ) { isVisible ->
        AnimatedLazyColumn(
            isVisible = isVisible,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            animatedItem { ctx ->
                WarningCard(
                    modifier = Modifier
                        .animated(ctx),
                    title = stringResource(R.string.generic_tip),
                    icon = { innerModifier ->
                        Icon(
                            modifier = innerModifier,
                            painter = painterResource(R.drawable.ic_lightbulb),
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.versions_export_tip_1),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = stringResource(R.string.versions_export_tip_2),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = stringResource(R.string.versions_export_tip_3),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = stringResource(R.string.versions_export_tip_4),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = stringResource(R.string.versions_export_tip_5),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )
            }

            animatedItem { ctx ->
                Column(
                    modifier = Modifier
                        .animated(ctx)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    //MCBBS
                    TypeItem(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        title = stringResource(R.string.versions_export_type_mcbbs),
                        summary = stringResource(R.string.versions_export_type_mcbbs_summary, BuildKeys.LAUNCHER_SHORT_NAME),
                        icon = painterResource(R.drawable.img_chest),
                        onClick = { onTypeSelect(PackType.MCBBS) }
                    )

                    //Modrinth
                    TypeItem(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        title = stringResource(R.string.versions_export_type_modrinth),
                        summary = stringResource(R.string.versions_export_type_summary_common),
                        icon = painterResource(R.drawable.img_platform_modrinth),
                        onClick = { onTypeSelect(PackType.Modrinth) }
                    )

                    //CurseForge
                    TypeItem(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        title = stringResource(R.string.versions_export_type_curseforge),
                        summary = stringResource(R.string.versions_export_type_summary_common),
                        icon = painterResource(R.drawable.img_platform_curseforge),
                        onClick = { onTypeSelect(PackType.CurseForge) }
                    )
                }
            }
        }
    }
}


/**
 * 导出类型布局
 */
@Composable
private fun TypeItem(
    title: String,
    summary: String,
    icon: Painter,
    position: CardPosition,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = rememberSettingsCardShape(position = position)

    BackgroundCard(
        modifier = modifier,
        onClick = onClick,
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //图标
            Image(
                modifier = Modifier.size(28.dp),
                painter = icon,
                contentDescription = title,
                contentScale = ContentScale.FillBounds
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = summary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Icon(
                painter = painterResource(R.drawable.ic_arrow_right_rounded),
                contentDescription = null
            )
        }
    }
}