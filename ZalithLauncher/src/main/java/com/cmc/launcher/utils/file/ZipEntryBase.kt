package com.cmc.launcher.utils.file

interface ZipEntryBase {
    val name: String
    val isDirectory: Boolean
}