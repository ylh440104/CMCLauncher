package com.cmc.launcher.game.addons.modloader.fabriclike

import com.cmc.launcher.game.addons.modloader.AddonVersion

abstract class FabricLikeVersion(
    /** Minecraft 版本 */
    inherit: String,
    /** 加载器名称 */
    val loaderName: String,
    /** 加载器版本 */
    val version: String,
    /** 版本状态: true 为稳定版 (Quilt忽略此值) */
    val stable: Boolean = true
) : AddonVersion(
    inherit = inherit
) {
    abstract val loaderUrl: String

    /**
     * 获取对应版本的版本 Json 下载地址
     */
    val loaderJsonUrl: String =
        "$loaderUrl/${
            inherit.replace("∞", "infinite")
        }/$version/profile/json"

    override fun getAddonVersion(): String = this.version

    override fun isVersion(versionString: String): Boolean {
        return this.version == versionString
    }
}