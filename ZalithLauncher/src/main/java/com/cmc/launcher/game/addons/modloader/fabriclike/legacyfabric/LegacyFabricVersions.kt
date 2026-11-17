package com.cmc.launcher.game.addons.modloader.fabriclike.legacyfabric

import com.cmc.launcher.game.addons.modloader.fabriclike.FabricLikeVersions
import com.cmc.launcher.game.addons.modloader.fabriclike.models.FabricLikeLoader

object LegacyFabricVersions : FabricLikeVersions(
    officialUrl = "https://meta.legacyfabric.net/v2"
) {
    /**
     * 获取 Fabric 列表
     */
    suspend fun fetchFabricLoaderList(mcVersion: String, force: Boolean = false): List<LegacyFabricVersion>? {
        val list: List<FabricLikeLoader> = fetchLoaderList(force, "LegacyFabricVersions", mcVersion) ?: return null

        return list.map { loader ->
            LegacyFabricVersion(
                inherit = mcVersion,
                version = loader.version,
                stable = loader.stable
            )
        }
    }
}