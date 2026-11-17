package com.cmc.launcher.game.account.auth_server

class ResponseException(
    val responseMessage: String,
    val statusCode: Int? = null
) : RuntimeException(
    responseMessage
)
