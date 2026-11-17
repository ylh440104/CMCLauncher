package com.cmc.launcher.ui.screens.content.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.cardgrid.model.CardLimits
import com.movtery.cardgrid.model.CardType
import com.cmc.launcher.BuildKeys
import com.cmc.launcher.R
import com.cmc.launcher.ui.components.BackgroundCard
import com.cmc.launcher.ui.screens.content.home.server.ServerCardContent
import com.cmc.launcher.ui.screens.content.home.version.VersionCardContent

/** 系统卡片（不可变更），由启动器自行提供并绘制在网格之外 */
class SystemCard(val id: String, val content: @Composable () -> Unit)

/**
 * 用户卡片图标的圆角
 */
val CardIconShape = RoundedCornerShape(percent = 28)

/**
 * 主页卡片注册表
 */
object HomeCards {
    /** 版本卡片的类型 id */
    const val VERSION_CARD_TYPE_ID = "version_card"

    /** 服务器卡片的类型 id */
    const val SERVER_CARD_TYPE_ID = "server_card"

    private val versionCardType = CardType(
        typeId = VERSION_CARD_TYPE_ID,
        defaultSpan = IntOffset(10, 6),
        limits = CardLimits(
            minWidth = 10,
            minHeight = 4,
            maxHeight = 12
        ),
        content = { cardId ->
            VersionCardContent(cardId)
        }
    )

    /** 服务器卡片类型 */
    private val serverCardType = CardType(
        typeId = SERVER_CARD_TYPE_ID,
        defaultSpan = IntOffset(10, 6),
        limits = CardLimits(
            minWidth = 10,
            minHeight = 4,
            maxHeight = 12
        ),
        content = { cardId ->
            ServerCardContent(cardId)
        }
    )

    /** 版本卡片类型 */
    fun versionCardType(): CardType = versionCardType

    /** 服务器卡片类型 */
    fun serverCardType(): CardType = serverCardType

    /** 用户卡片类型注册表 */
    val userCardTypes: List<CardType> = listOf(versionCardType, serverCardType)

    /** 系统卡片（不可变更） */
    fun systemCards(): List<SystemCard> = emptyList()
}
