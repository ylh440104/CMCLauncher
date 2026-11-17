package com.cmc.launcher.game.addons.modloader.fabriclike.fabric

import com.cmc.launcher.game.addons.modloader.ModLoader
import com.cmc.launcher.game.addons.modloader.fabriclike.FabricLikeVersion

class FabricVersion(
    inherit: String,
    version: String,
    stable: Boolean
) : FabricLikeVersion(
    inherit = inherit,
    loaderName = ModLoader.FABRIC.displayName,
    version = version,
    stable = stable
) {
    override val loaderUrl: String
        get() = "${FabricVersions.officialUrl}/versions/loader"
}