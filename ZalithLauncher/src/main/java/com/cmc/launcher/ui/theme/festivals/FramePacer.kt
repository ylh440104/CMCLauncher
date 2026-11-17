package com.cmc.launcher.ui.theme.festivals

/**
 * 帧步进限制器：以 60fps 为上限，决定每个 vsync 是否步进渲染。
 *
 * 在高于 60Hz 的屏幕上跳过多余的 vsync；判定采用余量携带方式，
 * 使 90Hz 等非整数倍刷新率的屏幕也能平均保持 60fps。
 * 纯 Kotlin 实现，可在 JVM 单元测试中运行。
 */
internal class FramePacer(
    private val targetFrameNanos: Long = DEFAULT_TARGET_FRAME_NANOS,
    private val skewNanos: Long = 1_500_000L
) {
    private var lastVsyncNanos = Long.MIN_VALUE
    private var lastStepNanos = Long.MIN_VALUE

    /** 报告一次 vsync；返回本次应步进的间隔（纳秒），返回 null 表示本帧跳过 */
    fun onVsync(frameNanos: Long): Long? {
        if (lastVsyncNanos == Long.MIN_VALUE) {
            lastVsyncNanos = frameNanos
            lastStepNanos = frameNanos
            return 0L
        }

        val elapsed = frameNanos - lastVsyncNanos
        if (elapsed < targetFrameNanos - skewNanos) return null

        // 余量带入下一帧判定，避免非整数倍屏幕上的节奏抖动
        lastVsyncNanos = frameNanos - (elapsed - targetFrameNanos).coerceAtMost(targetFrameNanos)
        val deltaNanos = frameNanos - lastStepNanos
        lastStepNanos = frameNanos
        return deltaNanos
    }

    /** 丢弃累计的余量，从头开始判定（恢复渲染时调用） */
    fun reset() {
        lastVsyncNanos = Long.MIN_VALUE
        lastStepNanos = Long.MIN_VALUE
    }

    companion object {
        const val DEFAULT_TARGET_FRAME_NANOS = 1_000_000_000L / 60L
    }
}
