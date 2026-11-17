package com.cmc.launcher.filemanager.logic.ops

import com.cmc.launcher.filemanager.os.FmLog
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions

private const val TAG = "FmFilePerms"

object FilePermissions {
    private val FILE_PERMS: Set<PosixFilePermission> = PosixFilePermissions.fromString("rw-rw-r--")
    private val DIR_PERMS: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwxrwxr-x")

    fun apply(path: Path) {
        try {
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) return

            val isDir = Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
            val permissions = if (isDir) DIR_PERMS else FILE_PERMS
            Files.setPosixFilePermissions(path, permissions)
        } catch (_: UnsupportedOperationException) {
            //文件系统不支持 POSIX 权限，忽略
        } catch (e: Exception) {
            FmLog.warn(TAG, "Failed to set permissions on $path", e)
        }
    }
}
