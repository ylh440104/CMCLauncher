package com.cmc.launcher.game.download.game.fabric

import com.cmc.launcher.coroutine.Task
import com.cmc.launcher.game.addons.mirror.mapBMCLMirrorUrls
import com.cmc.launcher.game.addons.modloader.fabriclike.FabricLikeVersion
import com.cmc.launcher.utils.file.ensureParentDirectory
import com.cmc.launcher.utils.network.fetchStringFromUrls
import kotlinx.coroutines.Dispatchers
import java.io.File

const val FABRIC_LIKE_DOWNLOAD_ID = "Download.FabricLike"

fun getFabricLikeDownloadTask(
    fabricLikeVersion: FabricLikeVersion,
    tempVersionJson: File
): Task {
    return Task.runTask(
        id = FABRIC_LIKE_DOWNLOAD_ID,
        dispatcher = Dispatchers.IO,
        task = {
            //下载版本 Json
            val loaderJson = fetchStringFromUrls(fabricLikeVersion.loaderJsonUrl.mapBMCLMirrorUrls())
            tempVersionJson
                .ensureParentDirectory()
                .writeText(loaderJson)
        }
    )
}