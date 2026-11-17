package com.cmc.launcher.game.version.installed

import java.io.File

enum class VersionFolders(val folderName: String) {
    /** 无目标文件夹 */
    NONE(""),
    /** 模组文件夹 */
    MOD("mods"),
    /** 资源包文件夹 */
    RESOURCE_PACK("resourcepacks"),
    /** 存档文件夹 */
    SAVES("saves"),
    /** 光影包文件夹 */
    SHADERS("shaderpacks"),
    /** 截屏文件夹 */
    SCREENSHOTS("screenshots");

    fun getDir(gameDir: String): String {
        return File(gameDir, folderName).absolutePath
    }

    fun getDir(gameDir: File): File {
        return File(gameDir, folderName)
    }
}