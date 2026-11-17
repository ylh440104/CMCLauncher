package com.cmc.launcher.setting.enums

import com.cmc.launcher.R

enum class MirrorSourceType(val textRes: Int) {
    /**
     * 自动选择下载源
     * 依据当前网络环境判定初始偏好，后续由下载引擎按失败情况自适应换源
     */
    AUTO(R.string.settings_launcher_mirror_auto),

    /**
     * 尽量使用官方源
     */
    OFFICIAL(R.string.settings_launcher_mirror_official),

    /**
     * 尽量使用镜像源
     */
    MIRROR(R.string.settings_launcher_mirror_mirror);

    companion object {
        /** 旧版枚举名到现名的映射，读取历史存档设置时兜底 */
        val LEGACY_NAMES: Map<String, MirrorSourceType> = mapOf(
            "OFFICIAL_FIRST" to OFFICIAL,
            "MIRROR_FIRST" to MIRROR
        )
    }
}