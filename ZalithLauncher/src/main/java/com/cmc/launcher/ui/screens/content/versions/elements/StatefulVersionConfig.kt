package com.cmc.launcher.ui.screens.content.versions.elements

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cmc.launcher.game.version.installed.VersionConfig
import java.io.File

class StatefulVersionConfig(private val config: VersionConfig) {
    val versionPath: File = config.getVersionPath()

    var isolationType by mutableStateOf(config.isolationType)
    var skipGameIntegrityCheck by mutableStateOf(config.skipGameIntegrityCheck)
    var javaRuntime by mutableStateOf(config.javaRuntime)
    var jvmArgs by mutableStateOf(config.jvmArgs)
    var gameArgs by mutableStateOf(config.gameArgs)
    var renderer by mutableStateOf(config.renderer)
    var sfpewPluginLayer by mutableStateOf(config.sfpewPluginLayer)
    var sfpewRelaxedContext by mutableStateOf(config.sfpewRelaxedContext)
    var driver by mutableStateOf(config.driver)
    var graphicsApi by mutableStateOf(config.graphicsApi)
    var control by mutableStateOf(config.control)
    var customInfo by mutableStateOf(config.customInfo)
    var serverIp by mutableStateOf(config.serverIp)
    var ramAllocation by mutableIntStateOf(config.ramAllocation)
    var touchVibrateKind by mutableStateOf(config.touchVibrateKind)
    var touchVibrateDuration by mutableIntStateOf(config.touchVibrateDuration)

    /**
     * 将当前状态重新打包为新的 [VersionConfig] 对象
     */
    fun pack(): VersionConfig = VersionConfig(versionPath).also { packed ->
        packInto(packed)
        packed.customPath = config.customPath
        packed.versionSummary = config.versionSummary
    }

    /**
     * 将当前状态写入已有的 [VersionConfig] 对象
     */
    fun packInto(target: VersionConfig) {
        target.isolationType = isolationType
        target.skipGameIntegrityCheck = skipGameIntegrityCheck
        target.javaRuntime = javaRuntime
        target.jvmArgs = jvmArgs
        target.gameArgs = gameArgs
        target.renderer = renderer
        target.sfpewPluginLayer = sfpewPluginLayer
        target.sfpewRelaxedContext = sfpewRelaxedContext
        target.driver = driver
        target.graphicsApi = graphicsApi
        target.control = control
        target.customInfo = customInfo
        target.serverIp = serverIp
        target.ramAllocation = ramAllocation
        target.touchVibrateKind = touchVibrateKind
        target.touchVibrateDuration = touchVibrateDuration
    }

    /**
     * @return 托管字段自创建以来是否被修改过
     */
    fun isModified(): Boolean = config.isModified(pack())
}
