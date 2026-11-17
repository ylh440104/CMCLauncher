package com.cmc.launcher.filemanager.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cmc.launcher.R
import com.cmc.launcher.utils.file.InvalidFilenameException
import com.cmc.launcher.utils.file.checkFilenameValidity

/**
 * 实时校验文件名合法性，返回本地化错误提示
 * @return 合法时返回 null，不合法时返回本地化错误字符串
 */
@Composable
fun fmFilenameInvalid(str: String): String? {
    return try {
        checkFilenameValidity(str)
        null
    } catch (e: InvalidFilenameException) {
        e.fmInvalidFilenameSummary()
    }
}

@Composable
fun InvalidFilenameException.fmInvalidFilenameSummary(): String = when {
    containsIllegalCharacters() -> stringResource(R.string.generic_input_invalid_character, illegalCharacters ?: "")
    isInvalidLength -> stringResource(R.string.file_invalid_length, invalidLength, 255)
    isLeadingOrTrailingSpace -> stringResource(R.string.file_invalid_leading_or_trailing_space)
    else -> ""
}
