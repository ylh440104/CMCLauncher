package com.cmc.launcher.game.versioninfo.models

import com.cmc.launcher.game.versioninfo.AprilFoolsVersion
import com.cmc.launcher.game.versioninfo.MinecraftVersion
import com.cmc.launcher.game.versioninfo.allAprilFools

/**
 * 将 [VersionManifest.Version] 列表映射为 [MinecraftVersion] 列表
 */
fun List<VersionManifest.Version>.mapVersion(): List<MinecraftVersion> {
    return this.map { version ->
        //检查其是否为愚人节版
        val aprilFoolsVersion = allAprilFools.find { it.version.equals(version.id, ignoreCase = true) }

        MinecraftVersion(
            version = version,
            type = checkVersionType(version.type, aprilFoolsVersion),
            summary = aprilFoolsVersion?.type?.summary, //暂时仅为愚人节版提供描述
            urlSuffix = aprilFoolsVersion?.type?.urlSuffix
        )
    }
}

/**
 * 尝试检查一个 Minecraft 版本的发行类型
 */
fun checkVersionType(
    versionId: String,
    type: String?,
): MinecraftVersion.Type {
    //检查其是否为愚人节版
    val aprilFoolsVersion = allAprilFools.find { it.version.equals(versionId, ignoreCase = true) }

    return checkVersionType(
        type = type,
        aprilFoolsVersion = aprilFoolsVersion
    )
}

private fun checkVersionType(
    type: String?,
    aprilFoolsVersion: AprilFoolsVersion?
): MinecraftVersion.Type {
    return if (aprilFoolsVersion != null) {
        //确认为愚人节版本
        MinecraftVersion.Type.AprilFools
    } else {
        when (type) {
            "release" -> MinecraftVersion.Type.Release
            "snapshot", "pending", "unobfuscated" -> MinecraftVersion.Type.Snapshot
            "old_beta" -> MinecraftVersion.Type.OldBeta
            "old_alpha" -> MinecraftVersion.Type.OldAlpha
            else -> MinecraftVersion.Type.Unknown
        }
    }
}

/**
 * 检查版本类型是否匹配给定的类型
 * @param release 如果该版本为正式版，则返回它的值
 * @param snapshot 如果该版本为快照版，则返回它的值
 * @param aprilFools 如果该版本为愚人节版，则返回它的值
 * @param old 如果该版本为远古版，则返回它的值
 */
fun MinecraftVersion.isType(
    release: Boolean,
    snapshot: Boolean,
    aprilFools: Boolean,
    old: Boolean
) = when (type) {
    MinecraftVersion.Type.Release -> release
    MinecraftVersion.Type.Snapshot -> snapshot
    MinecraftVersion.Type.OldBeta -> old
    MinecraftVersion.Type.OldAlpha -> old
    MinecraftVersion.Type.AprilFools -> aprilFools
    MinecraftVersion.Type.Unknown -> old //未知版本，默认归类到远古版
}