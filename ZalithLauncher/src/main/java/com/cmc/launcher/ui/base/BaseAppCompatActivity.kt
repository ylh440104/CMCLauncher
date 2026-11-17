package com.cmc.launcher.ui.base

import android.app.ActivityManager
import android.os.Bundle
import androidx.annotation.CallSuper
import com.cmc.launcher.BuildKeys
import com.cmc.launcher.context.refreshContext
import com.cmc.launcher.game.account.AccountsManager
import com.cmc.launcher.game.path.GamePathManager
import com.cmc.launcher.setting.loadAllSettings
import com.cmc.launcher.utils.checkStoragePermissionsForInit
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseAppCompatActivity : FullScreenAppCompatActivity() {
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        refreshContext(this)
        checkStoragePermissions()
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        loadAllSettings(this, true)
        checkStoragePermissions()
        refreshTaskDescription()
    }

    /**
     * @return 该 Activity 在最近任务中显示的标题
     */
    protected open fun getTaskDescriptionTitle(): String? = null

    @Suppress("DEPRECATION")
    private fun refreshTaskDescription() {
        val title = getTaskDescriptionTitle() ?: BuildKeys.LAUNCHER_NAME
        setTaskDescription(ActivityManager.TaskDescription(title))
    }

    protected fun refreshData() {
        AccountsManager.reloadAccounts()
        AccountsManager.reloadAuthServers()
        GamePathManager.reloadPath()
    }

    private fun checkStoragePermissions() {
        //检查所有文件管理权限
        checkStoragePermissionsForInit(this)
    }

    protected fun runFinish() = run { finish() }
}