package com.cmc.launcher.filemanager.logic

import com.cmc.launcher.utils.file.InvalidFilenameException
import com.cmc.launcher.utils.file.checkFilenameValidity

object FilenameValidator {
    /**
     * 校验文件名合法性
     * @throws FmFilenameException 非法时抛出，携带错误类别与细节
     */
    @Throws(FmFilenameException::class)
    fun check(name: String) {
        try {
            checkFilenameValidity(name)
        } catch (e: InvalidFilenameException) {
            throw FmFilenameException(
                message = e.message ?: "Invalid filename",
                type = when {
                    e.isLeadingOrTrailingSpace -> FmFilenameError.LEADING_OR_TRAILING_SPACE
                    e.isInvalidLength -> FmFilenameError.INVALID_LENGTH
                    else -> FmFilenameError.ILLEGAL_CHARACTERS
                },
                invalidLength = if (e.isInvalidLength) e.invalidLength else -1,
                illegalCharacters = if (e.containsIllegalCharacters()) e.illegalCharacters else null
            )
        }
    }

    /**
     * 验证文件名合法性
     * @return 校验失败时的错误异常，合法时为 null
     */
    fun verify(name: String): FmFilenameException? {
        return try {
            check(name)
            null
        } catch (e: FmFilenameException) {
            e
        }
    }
}
