package com.cmc.launcher.ui.screens.content.versions.elements

import com.cmc.launcher.game.version.resource_pack.ResourcePackInfo

/** 资源包操作状态 */
sealed interface ResourcePackOperation {
    data object None : ResourcePackOperation
    /** 执行任务中 */
    data object Progress : ResourcePackOperation
    /** 重命名资源包输入对话框 */
    data class RenamePack(val packInfo: ResourcePackInfo) : ResourcePackOperation
    /** 删除资源包输入对话框 */
    data class DeletePack(val packInfo: ResourcePackInfo) : ResourcePackOperation
}

/**
 * 简易的资源包过滤器
 */
data class ResourcePackFilter(
    val onlyShowValid: Boolean,
    val filterName: String
)

/**
 * 简易过滤器，过滤指定名称的资源包
 */
fun List<ResourcePackInfo>.filterPacks(filter: ResourcePackFilter) = this.filter {
    val valid = !filter.onlyShowValid || it.isValid
    val nameMatched = filter.filterName.isEmpty() ||
            //用清除了格式化代码的名称进行判断
            it.rawName.contains(filter.filterName, true)
    valid && nameMatched
}
