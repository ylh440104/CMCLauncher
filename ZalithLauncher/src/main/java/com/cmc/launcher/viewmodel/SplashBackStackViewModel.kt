package com.cmc.launcher.viewmodel

import androidx.lifecycle.ViewModel
import com.cmc.launcher.ui.screens.NestedNavKey

class SplashBackStackViewModel: ViewModel() {
    /** 启动屏幕 */
    val splashScreen = NestedNavKey.Splash()
}