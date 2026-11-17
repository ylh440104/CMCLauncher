package com.cmc.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cmc.launcher.coroutine.TaskLogOutput
import com.cmc.launcher.ui.AndroidStringText

/**
 * 任务流对话框的实时日志卡片
 */
@Composable
fun TaskLogCard(
    logOutput: TaskLogOutput,
    modifier: Modifier = Modifier
) {
    val lines by logOutput.lines.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var followScroll by remember { mutableStateOf(true) }

    // 用户向上翻阅时暂停自动滚动，重新滚到底部后恢复跟随
    LaunchedEffect(listState) {
        var previousIndex = 0
        snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
            if (index < previousIndex) {
                followScroll = false
            } else if (!listState.canScrollForward) {
                followScroll = true
            }
            previousIndex = index
        }
    }

    LaunchedEffect(lines.size) {
        if (followScroll && lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.lastIndex)
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AndroidStringText(
            text = logOutput.title,
            style = MaterialTheme.typography.titleMedium
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(color = MaterialTheme.colorScheme.surfaceContainer),
            state = listState,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
        ) {
            items(lines) { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
