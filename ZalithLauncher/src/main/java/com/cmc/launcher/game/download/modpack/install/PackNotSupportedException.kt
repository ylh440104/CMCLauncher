package com.cmc.launcher.game.download.modpack.install

/**
 * 整合包不受支持，无法导入时，抛出这个异常
 * @param reason 不受支持的原因
 */
class PackNotSupportedException(
    val reason: UnsupportedPackReason
) : RuntimeException(
    reason.reasonText
)

/**
 * 导致启动器判断整合包不受支持的原因
 */
enum class UnsupportedPackReason(
    val reasonText: String
) {
    /**
     * 压缩包损坏或解压失败
     */
    CorruptedArchive("The archive is corrupted or failed to extract."),

    /**
     * 不支持的整合包格式
     */
    UnsupportedFormat("The modpack format is not supported.")
}