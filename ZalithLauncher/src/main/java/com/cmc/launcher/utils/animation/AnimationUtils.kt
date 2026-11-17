package com.cmc.launcher.utils.animation

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween

/**
 * 获取通用动画的持续时长
 */
private fun getAnimateSpeed(): Int = SwapTimeline.time(SwapTimeline.CONTENT_BASE)

fun <E> getAnimateTween(
    delayMillis: Int = 0
): FiniteAnimationSpec<E> = getAnimateTween(
    durationMillis = getAnimateSpeed(),
    delayMillis = delayMillis
)

fun <E> getAnimateTween(
    durationMillis: Int,
    delayMillis: Int = 0
): FiniteAnimationSpec<E> = tween(
    durationMillis = durationMillis,
    delayMillis = delayMillis
)

fun <E> getEnterTween(
    delayMillis: Int = 0
): FiniteAnimationSpec<E> = tween(
    durationMillis = getAnimateSpeed(),
    delayMillis = delayMillis,
    easing = EnterTranslateEasing
)