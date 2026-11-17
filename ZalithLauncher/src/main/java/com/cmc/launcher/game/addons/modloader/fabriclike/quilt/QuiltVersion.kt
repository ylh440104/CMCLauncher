package com.cmc.launcher.game.addons.modloader.fabriclike.quilt

import com.cmc.launcher.game.addons.modloader.ModLoader
import com.cmc.launcher.game.addons.modloader.fabriclike.FabricLikeVersion

class QuiltVersion(
    inherit: String,
    version: String
) : FabricLikeVersion(
    inherit = inherit,
    loaderName = ModLoader.QUILT.displayName,
    version = version,
    /**
     * Quilt 没有在 Json 中提供 stable 键值
     * 只能通过版本名称是否带有 'beta' 来判断
     */
    stable = version.contains("beta")
) {
    override val loaderUrl: String
        get() = "${QuiltVersions.officialUrl}/versions/loader"
}