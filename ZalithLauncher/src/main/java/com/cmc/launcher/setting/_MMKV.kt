package com.cmc.launcher.setting

import com.cmc.launcher.BuildKeys
import com.tencent.mmkv.MMKV

/**
 * 启动器全局 MMKV，管理所有设置项
 */
fun launcherMMKV(): MMKV = MMKV.mmkvWithID(BuildKeys.LAUNCHER_IDENTIFIER, MMKV.MULTI_PROCESS_MODE)