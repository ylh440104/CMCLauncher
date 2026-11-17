package com.cmc.launcher.filemanager.ui

import androidx.navigation3.runtime.NavBackStack
import java.nio.file.Path

/**
 * 进入回收站页
 */
fun NavBackStack<FmNavKey>.openTrash() {
    if (lastOrNull() == FmNavKey.Trash) return
    add(FmNavKey.Trash)
}

/**
 * 退出回收站页返回主页面
 */
fun NavBackStack<FmNavKey>.closeTrash() {
    if (lastOrNull() == FmNavKey.Trash) {
        removeLastOrNull()
    }
}

/**
 * 进入文本编辑器页
 * @param path 待编辑文件的绝对路径
 */
fun NavBackStack<FmNavKey>.openEditor(path: Path) {
    if (lastOrNull() is FmNavKey.Editor) return
    add(FmNavKey.Editor(path.toString()))
}

/**
 * 退出文本编辑器页返回主页面
 */
fun NavBackStack<FmNavKey>.closeEditor() {
    if (lastOrNull() is FmNavKey.Editor) {
        removeLastOrNull()
    }
}
