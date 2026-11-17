package com.cmc.launcher.game.addons.modloader.fabriclike.legacyfabric

import com.cmc.launcher.game.addons.modloader.ModLoader
import com.cmc.launcher.game.addons.modloader.fabriclike.FabricLikeVersion

class LegacyFabricVersion(
    inherit: String,
    version: String,
    stable: Boolean
): FabricLikeVersion(
    inherit = inherit,
    loaderName = ModLoader.LEGACY_FABRIC_API.displayName,
    version = version,
    stable = stable
) {
    override val loaderUrl: String
        get() = "${LegacyFabricVersions.officialUrl}/versions/loader"
}