package com.cmc.launcher.ui.screens.splash

import com.cmc.launcher.ui.screens.TitledNavKey
import kotlinx.serialization.Serializable

sealed interface SplashOperation : TitledNavKey {
    @Serializable data object Loading : SplashOperation
    @Serializable data object Welcome : SplashOperation
    @Serializable data object Unpacking : SplashOperation
}
