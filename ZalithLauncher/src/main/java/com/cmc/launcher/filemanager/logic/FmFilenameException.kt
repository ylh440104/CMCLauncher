package com.cmc.launcher.filemanager.logic

/** 文件名错误类别 */
enum class FmFilenameError {
    /** 包含非法字符 / 路径穿越序列（如 `..`、开头 `/` 或 `\`） */
    ILLEGAL_CHARACTERS,

    /** 名称过长（超过 255 字符） */
    INVALID_LENGTH,

    /** 以空格开头或结尾 */
    LEADING_OR_TRAILING_SPACE,

    /** 与同目录下已有条目重名 */
    NAME_CONFLICT
}

/**
 * 文件名校验异常
 * @param message 错误描述
 * @param type 错误类别
 * @param invalidLength 非法名称长度（[FmFilenameError.INVALID_LENGTH] 时有效，否则为 -1）
 * @param illegalCharacters 命中的非法字符（[FmFilenameError.ILLEGAL_CHARACTERS] 时有效，否则为 null）
 */
class FmFilenameException(
    message: String,
    val type: FmFilenameError,
    val invalidLength: Int = -1,
    val illegalCharacters: String? = null
) : Exception(message)
