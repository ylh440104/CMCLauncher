package com.cmc.launcher.ui.vulkan_checker

import com.cmc.launcher.game.version.installed.Version
import com.cmc.launcher.utils.device.VulkanCapabilities

/**
 * Vulkan 检查器 UI 操作状态
 */
sealed interface VCOperation {
    data object None: VCOperation

    /**
     * Vulkan 检查提示对话框
     */
    data class Tip(val version: Version): VCOperation

    /**
     * @param data 检查结果
     * @param useTurnip 是否使用了 Turnip
     */
    data class Result(
        val data: VulkanCapabilities?,
        val useTurnip: Boolean = false
    ): VCOperation
}