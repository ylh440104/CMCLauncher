package com.cmc.launcher.game.download.game.forge

import com.cmc.launcher.coroutine.Task
import com.cmc.launcher.game.addons.mirror.mapBMCLMirrorUrls
import com.cmc.launcher.game.addons.modloader.forgelike.ForgeLikeVersion
import com.cmc.launcher.game.addons.modloader.forgelike.forge.ForgeVersion
import com.cmc.launcher.game.addons.modloader.forgelike.forge.ForgeVersions
import com.cmc.launcher.game.addons.modloader.forgelike.neoforge.NeoForgeVersion
import com.cmc.launcher.game.addons.modloader.forgelike.neoforge.NeoForgeVersions
import com.cmc.launcher.utils.network.downloadFileFromSources
import com.cmc.launcher.utils.network.withSpeedReport
import java.io.File

const val FORGE_LIKE_DOWNLOAD_ID = "Download.ForgeLike"

fun targetTempForgeLikeInstaller(tempGameDir: File): File = File(tempGameDir, ".temp/forge_like_installer.jar")

/**
 * 判断是否为 NeoForge 版本
 */
val ForgeLikeVersion.isNeoForge: Boolean
    get() = this is NeoForgeVersion

fun getForgeLikeDownloadTask(
    targetTempInstaller: File,
    forgeLikeVersion: ForgeLikeVersion
): Task {
    return Task.runTask(
        id = FORGE_LIKE_DOWNLOAD_ID,
        task = { task ->
            //获取安装器下载链接
            val url = if (forgeLikeVersion.isNeoForge) {
                NeoForgeVersions.getDownloadUrl(forgeLikeVersion as NeoForgeVersion)
            } else {
                ForgeVersions.getDownloadUrl(forgeLikeVersion as ForgeVersion)
            }

            withSpeedReport(
                onSpeedReport = { bytes ->
                    task.updateSpeed(bytes)
                },
                onClear = {
                    task.clearSpeed()
                }
            ) { report ->
                downloadFileFromSources(
                    urls = url.mapBMCLMirrorUrls(),
                    outputFile = targetTempInstaller,
                    sizeCallback = report
                )
            }
        }
    )
}