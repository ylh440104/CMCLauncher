package com.cmc.launcher.filemanager.viewmodel.controllers

import com.cmc.launcher.filemanager.logic.ops.DirectoryScanner
import com.cmc.launcher.filemanager.os.FmLog
import com.cmc.launcher.filemanager.viewmodel.DirScanUiState
import com.cmc.launcher.filemanager.viewmodel.FmStateStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.file.Path

private const val TAG = "FmDirectoryScan"

/** 目录属性扫描控制器 */
class DirectoryScanController(
    private val store: FmStateStore,
    private val coroutineScope: CoroutineScope
) {
    private var scanJob: Job? = null

    /** 启动对 [path] 的异步目录扫描 */
    fun startDirectoryScan(path: Path) {
        scanJob?.cancel()
        store.setDirScan(DirScanUiState(running = true, stats = null))
        val job = coroutineScope.launch(Dispatchers.IO) {
            val scanScope = this
            var lastUpdateMs = 0L
            try {
                val stats = DirectoryScanner.scan(
                    root = path,
                    onProgress = { s, _ ->
                        val now = System.currentTimeMillis()
                        if (now - lastUpdateMs >= 500) { // 0.5s 更新一次
                            lastUpdateMs = now
                            store.setDirScan(DirScanUiState(running = true, stats = s))
                        }
                    },
                    checkCancelled = { !scanScope.isActive }
                )
                store.setDirScan(DirScanUiState(running = false, stats = stats))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                FmLog.warn(TAG, "Directory scan failed: $path", e)
                store.setDirScan(DirScanUiState(running = false, stats = null))
            }
        }
        scanJob = job
    }

    fun stopDirectoryScan() {
        scanJob?.cancel()
        scanJob = null
        store.setDirScan(null)
    }
}
