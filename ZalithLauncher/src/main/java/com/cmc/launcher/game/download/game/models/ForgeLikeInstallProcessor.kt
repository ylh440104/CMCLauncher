package com.cmc.launcher.game.download.game.models

import com.google.gson.annotations.SerializedName

class ForgeLikeInstallProcessor(
    @SerializedName("sides")
    private val sides: List<String>?,
    @SerializedName("jar")
    private val jar: String,
    @SerializedName("classpath")
    private val classpath: List<String>?,
    @SerializedName("args")
    private val args: List<String>?,
    @SerializedName("outputs")
    private val outputs: Map<String, String>?
) {
    fun isSide(side: String): Boolean {
        return sides == null || sides.contains(side)
    }

    fun getJar(): LibraryComponents {
        return fromDescriptor(this.jar)
    }

    fun getClasspath(): List<LibraryComponents> {
        return classpath?.map { fromDescriptor(it) } ?: emptyList()
    }

    fun getArgs(): List<String> {
        return args ?: emptyList()
    }

    fun getOutputs(): Map<String, String> {
        return outputs ?: emptyMap()
    }
}