package com.cmc.launcher.ui.screens.game.multiplayer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.cmc.launcher.R
import com.cmc.launcher.terracotta.Terracotta
import com.cmc.launcher.ui.AndroidStringText
import com.cmc.launcher.ui.androidText
import com.cmc.launcher.ui.components.SimpleEditDialog
import com.cmc.launcher.utils.string.isEmptyOrBlank
import net.burningtnt.terracotta.TerracottaAndroidAPI

/** 等待中：房客操作状态 */
sealed interface GuestWaitingOperation {
    data object None : GuestWaitingOperation
    /** 被点击，开始输入邀请码 */
    data object OnClick : GuestWaitingOperation
}

@Composable
fun GuestWaitingOperation(
    operation: GuestWaitingOperation,
    onChange: (GuestWaitingOperation) -> Unit,
    onPositive: (roomCode: String) -> Unit,
    onShowToast: (AndroidStringText) -> Unit
) {
    when (operation) {
        is GuestWaitingOperation.None -> {}
        is GuestWaitingOperation.OnClick -> {
            InviteCodeInputDialog(
                onPositive = onPositive,
                onDismiss = {
                    onChange(GuestWaitingOperation.None)
                },
                onShowToast = onShowToast
            )
        }
    }
}

/**
 * 房客输入邀请码对话框
 */
@Composable
private fun InviteCodeInputDialog(
    onPositive: (roomCode: String) -> Unit,
    onDismiss: () -> Unit,
    onShowToast: (AndroidStringText) -> Unit
) {
    var code by remember { mutableStateOf("") }

    /** 验证不通过时 */
    var isError by remember { mutableStateOf(false) }
    val supportingText: AndroidStringText? = remember(code) {
        if (code.isEmpty()) {
            //还未填写内容
            isError = false
            return@remember null
        }

        val type = Terracotta.parseRoomCode(code)
        when (type) {
            TerracottaAndroidAPI.RoomType.TERRACOTTA_LEGACY -> androidText(R.string.terracotta_status_waiting_guest_prompt_terracotta_legacy)
            TerracottaAndroidAPI.RoomType.PCL2CE -> androidText(R.string.terracotta_status_waiting_guest_prompt_pcl2ce)
            TerracottaAndroidAPI.RoomType.SCAFFOLDING -> androidText(R.string.terracotta_status_waiting_guest_prompt_scaffolding)
            else -> null
        }.also { text ->
            //根据是否检测出对应格式判断
            isError = text == null
        } ?: androidText(R.string.terracotta_status_waiting_guest_prompt_invalid)
    }

    SimpleEditDialog(
        title = stringResource(R.string.terracotta_status_waiting_guest_prompt_title),
        value = code,
        onValueChange = { value ->
            code = value
        },
        label = (@Composable { Text(text = "U/XXXX-XXXX-XXXX-XXXX") }).takeIf { code.isEmpty() },
        supportingText = supportingText?.let { text ->
            {
                AndroidStringText(text = text)
            }
        },
        singleLine = true,
        isError = isError,
        onConfirm = {
            if (isError || code.isEmptyOrBlank() || Terracotta.parseRoomCode(code) == null) {
                onShowToast(androidText(R.string.terracotta_status_waiting_guest_prompt_invalid))
            } else {
                onPositive(code)
                onDismiss()
            }
        },
        onDismissRequest = onDismiss
    )
}