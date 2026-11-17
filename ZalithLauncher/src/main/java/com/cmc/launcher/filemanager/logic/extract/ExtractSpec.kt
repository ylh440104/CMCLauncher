package com.cmc.launcher.filemanager.logic.extract

/**
 * 解压选项。
 * @param independentFolder 是否解压到独立文件夹
 * @param password 解压密码
 */
data class ExtractOptions(
    val independentFolder: Boolean = true,
    val password: String? = null
)

/**
 * 解压结果汇总。
 */
data class ExtractSummary(
    val entryCount: Int
)
