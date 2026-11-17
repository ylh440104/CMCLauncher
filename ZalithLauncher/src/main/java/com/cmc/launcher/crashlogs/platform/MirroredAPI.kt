package com.cmc.launcher.crashlogs.platform

import com.cmc.launcher.crashlogs.AbstractAPI

object MirroredAPI: AbstractAPI(
    api = "https://api.logshare.cn/1/log",
    root = "https://logshare.cn"
)