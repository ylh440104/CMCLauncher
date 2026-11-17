package com.cmc.launcher.game.account

/**
 * 本地存储的登录凭据已被服务端拒绝
 * 无法通过刷新恢复，必须重新登录账号
 */
class CredentialsExpiredException : RuntimeException("Login credentials have expired")
