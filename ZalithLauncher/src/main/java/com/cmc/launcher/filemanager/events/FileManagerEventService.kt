package com.cmc.launcher.filemanager.events

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import com.cmc.launcher.filemanager.os.FmLog

private const val TAG = "FmEventService"

/**
 * 文件管理器事件服务，作为跨进程客户端与 [FileManagerEventBus] 之间的中转
 */
class FileManagerEventService : Service() {
    private val handler = Handler { msg ->
        when (msg.what) {
            FileManagerEvent.MSG_REGISTER_CLIENT -> msg.replyTo?.let {
                FileManagerEventBus.register(it)
                it.send(Message.obtain().apply { what = FileManagerEvent.MSG_REGISTER_CLIENT })
            }

            FileManagerEvent.MSG_UNREGISTER_CLIENT -> msg.replyTo?.let(FileManagerEventBus::unregister)
            else -> FmLog.warn(TAG, "Unknown message what=${msg.what}")
        }
        true
    }

    private val messenger = Messenger(handler)

    override fun onBind(intent: Intent?): IBinder? = messenger.binder

    companion object {
        const val ACTION_BIND = "com.cmc.launcher.filemanager.EVENT_SERVICE"
    }
}