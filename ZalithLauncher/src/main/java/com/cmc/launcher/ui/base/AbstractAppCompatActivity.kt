package com.cmc.launcher.ui.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.cmc.launcher.context.GlobalContext

abstract class AbstractAppCompatActivity : AppCompatActivity() {
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalContext = this
    }
}