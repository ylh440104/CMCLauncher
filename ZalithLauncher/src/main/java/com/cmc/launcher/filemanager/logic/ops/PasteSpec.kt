package com.cmc.launcher.filemanager.logic.ops

import java.nio.file.Path

/** 顶层冲突处理选项 */
enum class ConflictResolution {
    /** 跳过该项 */
    SKIP,
    /** 若为文件则完全覆盖，为文件夹则合并内容 */
    OVERWRITE,
    /** 为待写入条目追加后缀 */
    KEEP_BOTH
}

/** 粘贴模式。 */
enum class PasteMode {
    /** 复制 */
    COPY,
    /** 移动 */
    MOVE
}

/** 粘贴请求构建结果。 */
sealed interface PasteRequest {
    /** 不需要决策，可以直接执行 */
    data class Ready(val sources: List<Path>, val targetDir: Path, val mode: PasteMode) : PasteRequest

    /** 存在顶层冲突，需决策后才能执行 */
    data class ResolveRequest(
        val sources: List<Path>,
        val targetDir: Path,
        val mode: PasteMode,
        /** 与 [sources] 一一对应的冲突项，非空表示该项需要决策 */
        val conflicts: List<ConflictItem?>
    ) : PasteRequest
}

data class ConflictItem(val source: Path, val existing: Path)

/** 粘贴完成后的项级结果。 */
data class ItemResult(
    val source: Path,
    val target: Path?,
    val success: Boolean,
    val reason: String?
)

/** 整体粘贴结果。 */
data class PasteSummary(
    val mode: PasteMode,
    val targetDir: Path,
    val results: List<ItemResult>
)