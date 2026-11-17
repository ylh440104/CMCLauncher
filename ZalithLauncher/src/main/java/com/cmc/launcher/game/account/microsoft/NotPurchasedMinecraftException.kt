package com.cmc.launcher.game.account.microsoft

import com.cmc.launcher.R
import com.cmc.launcher.ui.AndroidStringText
import com.cmc.launcher.ui.androidText

class NotPurchasedMinecraftException : RuntimeException()

fun toLocal(): AndroidStringText {
    return androidText(R.string.account_logging_not_purchased_minecraft)
}
