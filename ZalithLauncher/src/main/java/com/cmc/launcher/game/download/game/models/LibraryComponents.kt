package com.cmc.launcher.game.download.game.models

import com.google.gson.annotations.SerializedName

data class LibraryComponents(
    @SerializedName("groupId")
    val groupId: String,
    @SerializedName("artifactId")
    val artifactId: String,
    @SerializedName("version")
    val version: String,
    @SerializedName("classifier")
    val classifier: String? = null,
    @SerializedName("extension")
    val extension: String = "jar"
) {
    val descriptor: String by lazy {
        // groupId:artifactId:version:classifier@extension
        "$groupId:$artifactId:$version".let {
            if (classifier.isNullOrEmpty()) it
            else "$it:$classifier"
        }.let {
            if (extension != "jar") "$it@$extension"
            else it
        }
    }

    override fun toString(): String {
        return descriptor
    }
}