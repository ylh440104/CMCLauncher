package com.cmc.launcher.game.download.game.optifine

/**
 * OptiFine 与 Forge 1.17+ 不兼容时抛出（需要 H1 Pre2 之后的 OptiFine）
 */
class OptiFineForge17IncompatibleException(
    /** 当前 OptiFine 的构建号（buildof.txt） */
    val buildof: String
) : RuntimeException(
    "OptiFine (build $buildof) is incompatible with Forge 1.17+, " +
            "OptiFine H1 Pre2 (20210924-190833) or newer is required"
)
