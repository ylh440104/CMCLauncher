package com.cmc.launcher.contract

import android.webkit.MimeTypeMap

/**
 * 将文件扩展名转化为 MimeType
 */
fun String.extensionToMimeType(): String {
    val extensionMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(this)
    return extensionMimeType ?: "*/*"
}