package com.cmc.launcher.filemanager.logic.ops

import com.cmc.launcher.filemanager.logic.AccessScope
import com.cmc.launcher.filemanager.logic.FilenameValidator
import com.cmc.launcher.filemanager.logic.FmFilenameError
import com.cmc.launcher.filemanager.logic.FmFilenameException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class RenameCreateOps(private val scope: AccessScope) {

    /** 重命名条目 */
    suspend fun rename(
        target: Path,
        newName: String
    ): Path {
        FilenameValidator.check(newName)
        val safeTarget = scope.guardAbsolute(target)
        if (!Files.exists(safeTarget, LinkOption.NOFOLLOW_LINKS)) {
            throw IOException("Target does not exist")
        }
        val parent = safeTarget.parent ?: throw IOException("Target has no parent")
        if (!Files.isWritable(parent)) throw IOException("Parent is not writable")
        val dest = parent.resolve(newName).normalize().toAbsolutePath()
        scope.guardAbsolute(dest)
        if (Files.exists(dest, LinkOption.NOFOLLOW_LINKS) && dest != safeTarget) {
            throw FmFilenameException("Name conflicts with existing entry", FmFilenameError.NAME_CONFLICT)
        }
        withContext(Dispatchers.IO) {
            try {
                Files.move(safeTarget, dest, StandardCopyOption.ATOMIC_MOVE)
            } catch (_: Exception) {
                Files.move(safeTarget, dest, StandardCopyOption.REPLACE_EXISTING)
            }
        }
        return dest
    }

    /** 新建文件夹 */
    suspend fun createFolder(parent: Path, name: String): Path = createEntry(parent, name, isFolder = true)

    /** 新建空白文件 */
    suspend fun createFile(parent: Path, name: String): Path = createEntry(parent, name, isFolder = false)

    private suspend fun createEntry(
        parent: Path,
        name: String,
        isFolder: Boolean
    ): Path {
        FilenameValidator.check(name)
        val safeParent = scope.guard(parent)
        if (!Files.isDirectory(safeParent)) throw IOException("Parent is not a directory")
        if (!Files.isWritable(safeParent)) throw IOException("Parent is not writable")

        val child = safeParent.resolve(name).normalize().toAbsolutePath()
        scope.guardAbsolute(child)

        if (Files.exists(child, LinkOption.NOFOLLOW_LINKS)) {
            throw FmFilenameException("Name conflicts with existing entry", FmFilenameError.NAME_CONFLICT)
        }

        withContext(Dispatchers.IO) {
            if (isFolder) {
                Files.createDirectories(child)
            } else {
                Files.createFile(child)
            }
            FilePermissions.apply(child)
        }

        return child
    }
}