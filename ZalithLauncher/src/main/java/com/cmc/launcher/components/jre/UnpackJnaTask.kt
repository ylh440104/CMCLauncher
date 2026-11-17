package com.cmc.launcher.components.jre

import android.content.Context
import com.cmc.launcher.components.AssetManifest
import com.cmc.launcher.components.ComponentFile
import com.cmc.launcher.components.UnpackSingleTask
import com.cmc.launcher.components.toComponentFile
import com.cmc.launcher.path.PathManager
import com.cmc.launcher.utils.device.Architecture

class UnpackJnaTask(context: Context) : UnpackSingleTask(
    context = context,
    rootDir = PathManager.DIR_JNA,
    assetsDirName = "runtimes/jna",
    fileDirName = "jna"
) {
    override fun selectFiles(manifest: AssetManifest): List<ComponentFile> {
        val abi = Architecture.archAsStringAndroid(Architecture.getDeviceArchitecture())
        val versions = manifest.files.mapTo(HashSet()) { it.path.substringBefore('/') }
        val files = manifest.files.mapNotNull { file ->
            val parts = file.path.split('/')
            require(parts.size == 4 && parts[1] == "natives" && parts[3] == "libjnidispatch.so") {
                "Unsupported JNA asset path: ${file.path}"
            }
            if (parts[2] != abi) return@mapNotNull null
            file.toComponentFile("${parts[0]}/libjnidispatch.so")
        }
        require(files.mapTo(HashSet()) { it.outputPath.substringBefore('/') } == versions) {
            "JNA assets are incomplete for ABI: $abi"
        }
        return files
    }
}