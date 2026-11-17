package com.cmc.launcher.ui.screens.content.versions.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.cmc.launcher.game.version.export.data.FileSelectionData

sealed interface VisibleNode {
    data class FileNode(
        val data: FileSelectionData,
        private val indentation0: Int
    ) : VisibleNode {
        override val indentation: Int get() = indentation0
        override val key: String get() = data.file.absolutePath
    }

    /** 仅展示空目录提示文本 */
    data class EmptyNode(
        private val key0: String,
        private val indentation0: Int,
    ) : VisibleNode {
        override val indentation: Int get() = indentation0
        override val key: String get() = key0
    }

    val indentation: Int
    val key: String
}

private data class StackItem(
    val node: FileSelectionData,
    val indentation: Int
)

@Composable
fun rememberVisibleNodes(
    list: List<FileSelectionData>,
    refreshExpand: Any? = null
): List<VisibleNode> {
    return remember(list, refreshExpand) {
        val result = ArrayList<VisibleNode>(list.size)
        val stack = ArrayDeque<StackItem>()

        for (i in list.indices.reversed()) {
            stack.addLast(
                StackItem(list[i], 0)
            )
        }

        while (stack.isNotEmpty()) {
            val (node, indentation) = stack.removeLast()

            result.add(
                VisibleNode.FileNode(node, indentation)
            )

            val child = node.child
            if (child != null && node.expand.value) {
                val indentation0 = indentation + 1

                if (child.isEmpty()) {
                    val key = "parent:" + node.file.absolutePath + ",indentation=" + indentation.toString()
                    result.add(
                        VisibleNode.EmptyNode(key, indentation0)
                    )
                } else {
                    for (i in child.indices.reversed()) {
                        stack.addLast(
                            StackItem(child[i], indentation0)
                        )
                    }
                }
            }
        }

        result
    }
}