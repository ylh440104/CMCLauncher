package com.cmc.launcher.filemanager.logic.compress

import java.nio.file.Path

/**
 * 压缩格式
 */
enum class CompressFormat(val extension: String, val mimeType: String) {
    ZIP("zip", "application/zip"),
    SEVEN_Z("7z", "application/x-7z-compressed"),
    TAR("tar", "application/x-tar");

    /** 输出文件名的标准后缀 */
    val suffix: String get() = ".$extension"

    /** 该格式的默认压缩方法 */
    val defaultMethod: CompressMethod
        get() = when (this) {
            ZIP -> CompressMethod.DEFLATE
            SEVEN_Z -> CompressMethod.LZMA2
            TAR -> CompressMethod.TAR_POSIX
        }
}

/**
 * 压缩方法
 */
enum class CompressMethod(val displayName: String) {
    /** Zip：不压缩直接存储；7Z：COPY 不压缩 */
    STORE("Store"),
    /** Zip 默认 */
    DEFLATE("Deflate"),

    /** 7Z 默认 */
    LZMA2("LZMA2"),
    BZIP2("BZIP2"),

    /** TAR 长文件名处理：GNU 格式 */
    TAR_GNU("GNU"),
    /** TAR 长文件名处理：POSIX（PAX）格式 */
    TAR_POSIX("POSIX");

    companion object {
        /** ZIP 支持的方法 */
        val zipMethods = listOf(STORE, DEFLATE)
        /** 7Z 支持的方法 */
        val sevenZMethods = listOf(LZMA2, BZIP2)
        /** TAR 支持的方法（长文件名处理格式） */
        val tarMethods = listOf(TAR_GNU, TAR_POSIX)
    }
}

/**
 * 压缩参数。
 * @param format 压缩格式
 * @param method 压缩方法，null 表示使用格式默认
 * @param level 压缩等级（1-9），null 表示格式默认
 * @param password 密码，TAR 不支持
 */
data class CompressOptions(
    val format: CompressFormat,
    val method: CompressMethod? = null,
    val level: Int? = null,
    val password: String? = null
)

/**
 * 压缩结果汇总。
 */
data class CompressSummary(
    val outputPath: Path,
    val entryCount: Int
)
