package com.cmc.launcher.game.multirt

import com.cmc.launcher.ZLApplication.Companion.DEVICE_ARCHITECTURE
import com.cmc.launcher.utils.device.Architecture.archAsInt

data class Runtime(
    val name: String,
    val versionString: String?,
    val arch: String?,
    val javaVersion: Int,
    val isProvidedByLauncher: Boolean,
    val isJDK8: Boolean
) {
    constructor(name: String) : this(
        name = name,
        versionString = null,
        arch = null,
        javaVersion = 0,
        isProvidedByLauncher = false,
        isJDK8 = false
    )

    fun isCompatible(): Boolean =
        versionString != null && DEVICE_ARCHITECTURE == archAsInt(arch)
}
