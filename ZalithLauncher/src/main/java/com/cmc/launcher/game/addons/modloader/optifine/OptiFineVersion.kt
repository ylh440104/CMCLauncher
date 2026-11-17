package com.cmc.launcher.game.addons.modloader.optifine

import com.cmc.launcher.game.addons.modloader.AddonVersion

class OptiFineVersion(
    /** 显示名称 */
    val displayName: String,
    /** 文件名称 */
    val fileName: String,
    /** 版本名称 */
    val version: String,
    /** Minecraft 版本 */
    inherit: String,
    /** 发布时间，格式为“yyyy/mm/dd” */
    val releaseDate: String,
    /** 最低需求 Forge 版本：null 为不兼容，空字符串为无限制 */
    val forgeVersion: String?,
    /** 是否为预览版本 */
    val isPreview: Boolean
) : AddonVersion(
    inherit = inherit
) {
    /**
     * OptiFine_1.10.2_HD_U_C1 -> C1
     * 1.20.6 J1 pre18 -> J1 pre18
     */
    val realVersion: String
        get() = displayName.removePrefix(inherit).trim()

    override fun getAddonVersion(): String = this.version

    override fun isVersion(versionString: String): Boolean {
        return this.version == versionString
    }

    /**
     * 从 OptiFine 文件名解析出 Maven 版本号
     * OptiFine_1.10.2_HD_U_C1.jar -> 1.10.2_HD_U_C1
     */
    fun mavenVersion(): String {
        return fileName
            .removePrefix("preview_")
            .removePrefix("OptiFine_")
            .removeSuffix(".jar")
    }

    /**
     * 判断是否与已安装信息匹配，兼容以下几种来源：
     * 规范版本串（launchFor 元数据）、Maven 坐标版本（外来安装的 libraries 识别）、旧版元数据的短版本串
     */
    fun matchesInstalledVersion(installedVersion: String): Boolean {
        return installedVersion == version
                || installedVersion == mavenVersion()
                || installedVersion == realVersion
    }
}