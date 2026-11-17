package com.cmc.launcher.game.account.auth_server.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class AuthRequest(
    @SerializedName("agent")
    @SerialName("agent")
    var agent: Agent,
    @SerializedName("username")
    @SerialName("username")
    var username: String,
    @SerializedName("password")
    @SerialName("password")
    var password: String,
    @SerializedName("clientToken")
    @SerialName("clientToken")
    var clientToken: String,
    @SerializedName("requestUser")
    @SerialName("requestUser")
    var requestUser: Boolean
) {
    @Serializable
    class Agent(
        @SerializedName("name")
        @SerialName("name")
        var name: String,
        @SerializedName("version")
        @SerialName("version")
        var version: Int
    )
}