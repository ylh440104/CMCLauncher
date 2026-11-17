package com.cmc.launcher.filemanager.viewmodel

import java.nio.file.Path

/** 文件管理器内部剪贴板 */
data class FmClipboard(
    /** 已登记的源条目绝对路径 */
    val sources: List<Path>,
    /** true 表示剪贴，false 表示复制 */
    val isCut: Boolean
)