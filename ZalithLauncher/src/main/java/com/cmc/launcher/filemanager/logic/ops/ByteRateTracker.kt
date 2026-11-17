package com.cmc.launcher.filemanager.logic.ops

/** 采样窗口 */
private const val WINDOW_NANOS = 500_000_000L

/** 实时速率跟踪器 */
class ByteRateTracker {
    private var lastSampleNanos = 0L
    private var lastBytes = 0L
    private var currentRate = 0L

    fun start() {
        lastSampleNanos = System.nanoTime()
        lastBytes = 0L
        currentRate = 0L
    }

    /**
     * 以当前累计字节数计算实时速率
     * @param cumulativeBytes 从任务开始到当前的累计处理字节数
     * @return 实时速率（bytes/s）
     */
    fun rate(cumulativeBytes: Long): Long {
        val now = System.nanoTime()
        val elapsed = now - lastSampleNanos
        if (elapsed >= WINDOW_NANOS) {
            val delta = cumulativeBytes - lastBytes
            if (delta >= 0) {
                currentRate = (delta * 1_000_000_000L) / elapsed
            }
            lastSampleNanos = now
            lastBytes = cumulativeBytes
        }
        return currentRate
    }
}
