package com.cmc.launcher.game.addons.modloader.forgelike.neoforge.models

import com.cmc.launcher.game.addons.modloader.forgelike.neoforge.NeoForgeVersion
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * NeoForged Maven
 */
@Serializable
data class NeoForgedMaven(
    @SerialName("isSnapshot")
    val isSnapshot: Boolean,
    @SerialName("versions")
    val versions: List<String>,
    @Transient
    val isLegacy: Boolean = false
): NeoForgeMergeableMaven<NeoForgedMaven> {

    override fun plus(maven: NeoForgedMaven): List<NeoForgeVersion> {
        return this.versions.mapNotNull { versionId ->
            if (isVersionInvalid(versionId)) return@mapNotNull null
            NeoForgeVersion(versionId, this.isLegacy)
        } + maven.versions.mapNotNull { versionId ->
            if (isVersionInvalid(versionId)) return@mapNotNull null
            NeoForgeVersion(versionId, maven.isLegacy)
        }
    }
}