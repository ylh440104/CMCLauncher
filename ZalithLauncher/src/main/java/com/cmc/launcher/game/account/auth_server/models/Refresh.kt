package com.cmc.launcher.game.account.auth_server.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Refresh(
    @SerializedName("selectedProfile")
    @SerialName("selectedProfile")
    var selectedProfile: SelectedProfile? = null,
    @SerializedName("accessToken")
    @SerialName("accessToken")
    var accessToken: String,
    @SerializedName("clientToken")
    @SerialName("clientToken")
    var clientToken: String
) {
    @Serializable
    class SelectedProfile(
        @SerializedName("name")
        @SerialName("name")
        var name: String,
        @SerializedName("id")
        @SerialName("id")
        var id: String
    )
}