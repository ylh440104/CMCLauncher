package com.cmc.launcher.ui.guide

import com.movtery.guide.GuideKey

/** 启动器各类引导 */
sealed interface GuideKeys : GuideKey {
    /** 一组引导（一条引导流的身份），用于跨层启动事件与进度存储 */
    sealed interface Keys {
        /** 该组引导的版本号 */
        val version: Int
    }

    /** 主界面引导 */
    data object Main : Keys {
        override val version = 1

        sealed interface Step : GuideKey {
            /** 添加账号入口 */
            data object Account : Step
            /** 版本列表入口 */
            data object VersionList : Step
            /** 可长按拖动换边的操作菜单卡片 */
            data object CardDrag : Step
            /** 卡片化自定义主界面引导 */
            data object CardTip : Step
        }
    }

    /** 控制布局编辑器的引导 */
    data object Editor : Keys {
        override val version = 2

        sealed interface Step : GuideKey {
            /** 悬浮菜单 */
            data object MenuBall : Step
            /** 控件层列表 */
            data object LayerList : Step
            /** 创建控件层 */
            data object CreateLayer : Step
            /** 创建控件 */
            data object AddButtons : Step
            /** 创建控件样式 */
            data object AddStyles : Step
            /** 样式收藏柜 */
            data object StyleCabinet : Step
            /** 预览控制布局 */
            data object Preview : Step
            /** 保存 */
            data object Save : Step
        }
    }

    /** 控制布局编辑器摇杆方向事件 */
    data object JoystickEvents : Keys {
        override val version = 1

        sealed interface Step : GuideKey {
            /** 摇杆8个方向 */
            data object Direction : Step
            /** 摇杆锁定事件 */
            data object Lock : Step
            /** 摇杆按键事件绑定 */
            data object Events : Step
        }
    }
}