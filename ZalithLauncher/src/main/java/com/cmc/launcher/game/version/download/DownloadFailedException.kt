package com.cmc.launcher.game.version.download

class DownloadFailedException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
