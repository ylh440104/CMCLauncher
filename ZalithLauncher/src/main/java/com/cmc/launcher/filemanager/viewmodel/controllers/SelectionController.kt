package com.cmc.launcher.filemanager.viewmodel.controllers

import com.cmc.launcher.filemanager.logic.entry.FmEntry
import com.cmc.launcher.filemanager.viewmodel.FmStateStore
import com.cmc.launcher.filemanager.viewmodel.entryPathKey

/** 主列表选择 / 多选控制器 */
class SelectionController(private val store: FmStateStore) {
    /**
     * 进入多选并选中该条目
     */
    fun enterMultiSelectWith(entry: FmEntry) {
        val key = entryPathKey(entry)
        store.rangeAnchorKey = key
        store.setSelection(store.selection + key, true)
    }

    /**
     * 在多选模式下点击条目，切换选中
     */
    fun toggleSelection(entry: FmEntry) {
        val key = entryPathKey(entry)
        val newSelection = if (key in store.selection) {
            store.selection - key
        } else {
            store.selection + key
        }
        if (newSelection.isEmpty()) {
            store.rangeAnchorKey = null
            store.setSelection(newSelection, false)
        } else {
            store.setSelection(newSelection, true)
        }
    }

    /**
     * 滑动连选
     * 以锚点与滑动项为边界，选中两者之间的未选中项
     */
    fun swipeRangeSelect(entry: FmEntry) {
        val list = store.stateValue().visibleEntries
        val swipeKey = entryPathKey(entry)
        val swipeIndex = list.indexOfFirst { entryPathKey(it) == swipeKey }
        if (swipeIndex < 0) return

        if (!store.stateValue().multiSelect) {
            enterMultiSelectWith(entry)
            return
        }

        val anchorIndex = store.rangeAnchorKey?.let { a ->
            list.indexOfFirst { entryPathKey(it) == a }
        } ?: -1
        if (anchorIndex < 0) {
            // 无锚点（框选已结束 / 锚点失效）
            // 以本次滑动项为新锚点，新一轮框选开始
            store.rangeAnchorKey = swipeKey
            store.setSelection(store.selection + swipeKey, true)
            return
        }

        val from = minOf(anchorIndex, swipeIndex)
        val to = maxOf(anchorIndex, swipeIndex)
        val newSelection = store.selection + list.subList(from, to + 1).map { entryPathKey(it) }
        // 本次框选完成，清除锚点，以便下一次滑动开始新一轮框选
        store.rangeAnchorKey = null
        store.setSelection(newSelection, true)
    }

    fun selectAll() {
        store.rangeAnchorKey = null
        store.setSelection(store.stateValue().visibleEntries.map { entryPathKey(it) }.toSet(), true)
    }

    fun clearSelection() {
        store.rangeAnchorKey = null
        store.setSelection(emptySet(), false)
    }
}
