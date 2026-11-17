package com.cmc.launcher.game.addons.modloader.modlike

import com.cmc.launcher.game.addons.modloader.AddonVersion
import com.cmc.launcher.game.download.addon.platform.modrinth.models.ModrinthFile
import com.cmc.launcher.game.download.addon.platform.modrinth.models.ModrinthVersion

/**
 * 模组版本，因 Modrinth 访问优势，暂只支持 Modrinth
 */
open class ModVersion(
    /** Minecraft 版本 */
    inherit: String,
    /** 显示名称 */
    val displayName: String,
    /** 版本详细信息类 */
    val version: ModrinthVersion,
    /** 可下载的主文件 */
    val file: ModrinthFile
) : AddonVersion(
    inherit = inherit
) {

    override fun getAddonVersion(): String = this.version.versionNumber

    override fun isVersion(versionString: String): Boolean {
        return this.version.versionNumber == versionString
    }
}