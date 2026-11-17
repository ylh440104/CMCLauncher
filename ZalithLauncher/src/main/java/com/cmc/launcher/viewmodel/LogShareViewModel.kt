package com.cmc.launcher.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.io.File

/**
 * 游戏日志分享菜单状态管理 ViewModel
 */
class LogShareViewModel : ViewModel() {
    /** 当前待分享的日志文件 */
    var currentLogFile by mutableStateOf<File?>(null)
        private set

    /** 是否显示日志操作菜单 */
    var showMenu by mutableStateOf(false)
        private set

    /**
     * 打开日志分享菜单
     */
    fun openMenu(logFile: File) {
        currentLogFile = logFile
        showMenu = true
    }

    /**
     * 关闭日志分享菜单
     */
    fun closeMenu() {
        showMenu = false
    }

    /**
     * 重置状态
     */
    fun reset() {
        showMenu = false
        currentLogFile = null
    }
}
