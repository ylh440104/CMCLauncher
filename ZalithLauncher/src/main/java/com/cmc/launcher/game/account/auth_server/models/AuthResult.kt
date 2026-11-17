package com.cmc.launcher.game.account.auth_server.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
class AuthResult(
    @SerializedName("accessToken")
    @SerialName("accessToken")
    val accessToken: String,
    @SerializedName("clientToken")
    @SerialName("clientToken")
    var clientToken: String,
    @SerializedName("availableProfiles")
    @SerialName("availableProfiles")
    var availableProfiles: List<AvailableProfiles>? = null,
    @SerializedName("user")
    @SerialName("user")
    var user: User? = null,
    @SerializedName("selectedProfile")
    @SerialName("selectedProfile")
    var selectedProfile: SelectedProfile? = null
) {
    @Serializable
    class User(
        @SerializedName("id")
        @SerialName("id")
        var id: String,
        @SerializedName("properties")
        @SerialName("properties")
        var properties: JsonElement? = null
    )

    @Serializable
    class SelectedProfile(
        @SerializedName("id")
        @SerialName("id")
        var id: String,
        @SerializedName("name")
        @SerialName("name")
        var name: String
    )

    @Serializable
    class AvailableProfiles(
        @SerializedName("id")
        @SerialName("id")
        var id: String,
        @SerializedName("name")
        @SerialName("name")
        var name: String
    )
}