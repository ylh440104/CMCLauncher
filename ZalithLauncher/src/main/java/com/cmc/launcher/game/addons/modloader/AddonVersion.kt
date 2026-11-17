package com.cmc.launcher.game.addons.modloader

abstract class AddonVersion(
    /** Minecraft 版本 */
    open val inherit: String
) {
    /**
     * 获取该组件的版本号
     */
    abstract fun getAddonVersion(): String

    /**
     * 检查组件的版本号是否符合要求
     */
    abstract fun isVersion(versionString: String): Boolean
}
