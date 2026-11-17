package com.cmc.launcher.game.plugin.renderer_v2.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 旧版渲染器环境变量配置，仅用于迁移版本文件夹下的 renderer.config
 */
@Serializable
data class RendererEnvConfig(
    /** 各渲染器插件的环境变量配置，键为插件包名 */
    @SerialName("renderers")
    val renderers: MutableMap<String, RendererEnvs> = mutableMapOf()
) {
    companion object {
        const val CONFIG_FILE_NAME = "renderer.config"
    }
}

/**
 * 单个渲染器插件的环境变量配置
 */
@Serializable
data class RendererEnvs(
    /** 环境变量取值，键为环境变量名 */
    @SerialName("envs")
    val envs: MutableMap<String, String> = mutableMapOf(),
    /** 环境变量的启用状态，键为环境变量名 */
    @SerialName("checks")
    val checks: MutableMap<String, Boolean> = mutableMapOf()
)