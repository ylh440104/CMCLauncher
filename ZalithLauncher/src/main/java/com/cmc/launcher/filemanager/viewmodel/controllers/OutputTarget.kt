package com.cmc.launcher.filemanager.viewmodel.controllers

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import java.nio.file.Path

/** 输出目标抽象 */
internal sealed interface OutputTarget {
    /** 本地目录（可访问范围目录内） */
    data class Local(val dir: Path) : OutputTarget
    /** SAF 树目录根 */
    data class Saf(val treeUri: Uri) : OutputTarget
    /** SAF 树目录下的具名子目录（独立文件夹） */
    data class SafDir(val treeUri: Uri, val name: String) : OutputTarget
}

/**
 * 在 SAF 树目录中按名称查找子文档
 * @return 存在则返回其 document URI
 */
internal fun findChildDocument(context: Context, treeUri: Uri, name: String): Uri? {
    return runCatching {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val child = DocumentsContract.buildDocumentUriUsingTree(treeUri, "$docId/$name")
        if (DocumentsContract.isDocumentUri(context, child)) {
            //存在性以 try 打开为准
            context.contentResolver.openInputStream(child)?.close()
            child
        } else null
    }.getOrNull()
}

/** 按文件扩展名推断 MIME 类型 */
internal fun guessMime(name: String): String {
    val lower = name.lowercase()
    return when {
        lower.endsWith(".zip") -> "application/zip"
        lower.endsWith(".7z") -> "application/x-7z-compressed"
        lower.endsWith(".tar") -> "application/x-tar"
        lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
        lower.endsWith(".png") -> "image/png"
        lower.endsWith(".gif") -> "image/gif"
        lower.endsWith(".json") -> "application/json"
        lower.endsWith(".txt") || lower.endsWith(".log") -> "text/plain"
        lower.endsWith(".xml") -> "text/xml"
        lower.endsWith(".html") || lower.endsWith(".htm") -> "text/html"
        else -> "application/octet-stream"
    }
}
