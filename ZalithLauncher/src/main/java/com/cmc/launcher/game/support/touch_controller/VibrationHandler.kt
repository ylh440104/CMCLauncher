package com.cmc.launcher.game.support.touch_controller

import android.annotation.SuppressLint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.google.gson.annotations.SerializedName
import com.cmc.launcher.utils.logging.Logger
import top.fifthlight.touchcontroller.proxy.client.LauncherProxyClient
import top.fifthlight.touchcontroller.proxy.message.VibrateMessage

private const val TAG = "VibrationHandler"

class VibrationHandler(
    private val vibrator: Vibrator,
    private val vibrateDuration: Int?,
    private val vibrateKind: VibrateKind?,
) : LauncherProxyClient.VibrationHandler {
    enum class VibrateKind {
        @SerializedName("ONE_SHOT")
        ONE_SHOT,
        @SerializedName("CLICK")
        CLICK,
        @SerializedName("DOUBLE_CLICK")
        DOUBLE_CLICK,
        @SerializedName("HEAVY_CLICK")
        HEAVY_CLICK,
        @SerializedName("TICK")
        TICK;

        companion object {
            val default = ONE_SHOT
        }
    }

    companion object {
        @SuppressLint("NewApi")
        fun vibrate(
            vibrator: Vibrator,
            vibrateDuration: Int?,
            vibrateKind: VibrateKind?,
        ) {
            runCatching {
                val effectKind = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    VibrateKind.ONE_SHOT
                } else {
                    vibrateKind
                }
                val effect = when (effectKind) {
                    VibrateKind.ONE_SHOT, null -> {
                        val duration = vibrateDuration?.coerceAtMost(500)?.coerceAtLeast(80)?.toLong()
                        VibrationEffect.createOneShot(
                            duration ?: 100L,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    }

                    VibrateKind.CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    VibrateKind.DOUBLE_CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                    VibrateKind.HEAVY_CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    VibrateKind.TICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                }
                vibrator.vibrate(effect)
            }.onFailure {
                Logger.error(TAG, "Failed to attempt vibrating the device!", it)
            }
        }
    }

    override fun vibrate(kind: VibrateMessage.Kind) = vibrate(vibrator, vibrateDuration, vibrateKind)
}