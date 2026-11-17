package com.cmc.launcher.path

import com.cmc.launcher.path.PathManager.Companion.DIR_COMPONENTS
import com.cmc.launcher.path.PathManager.Companion.DIR_JNA
import java.io.File

class LibPath {
    companion object {
        private val LAUNCHER_COMPONENTS = File(DIR_COMPONENTS, "launcher")
        private val AUTH_LIBS_DIR = File(DIR_COMPONENTS, "auth_libs")

        @JvmField val CACIO_8 = File(DIR_COMPONENTS, "caciocavallo")
        @JvmField val CACIO_17 = File(DIR_COMPONENTS, "caciocavallo17")
        @JvmField val CACIO_17_AGENT = File(CACIO_17, "cacio-agent.jar")

        @JvmField val JNA = File(DIR_JNA, "jna")

        @JvmField val MIO_LIB_PATCHER = File(LAUNCHER_COMPONENTS, "MioLibPatcher.jar")
        @JvmField val MIO_LAUNCH_WRAPPER = File(LAUNCHER_COMPONENTS, "MioLaunchWrapper.jar")
        /**
         * [Github](https://github.com/bangbang93/forge-install-bootstrapper)
         */
        @JvmField val FORGE_INSTALLER = File(LAUNCHER_COMPONENTS, "forge_installer.jar")
        /**
         * 供 Forge 1.13 ~ 1.16 的 ModLauncher 加载 OptiFine
         */
        @JvmField val TRANSFORMER_DISCOVERY_SERVICE = File(LAUNCHER_COMPONENTS, "HMCLTransformerDiscoveryService-1.0.jar")

        @JvmField val AUTHLIB_INJECTOR = File(AUTH_LIBS_DIR, "authlib-injector.jar")
        @JvmField val NIDE_8_AUTH = File(AUTH_LIBS_DIR, "nide8auth.jar")
    }
}