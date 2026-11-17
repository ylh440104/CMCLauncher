package com.cmc.launcher.game.download.jvm_server

class JvmCrashException(val code: Int) : RuntimeException("During installation, the JVM exited with exception code $code.")