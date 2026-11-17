package com.cmc.launcher.crashlogs.platform

import com.cmc.launcher.crashlogs.AbstractAPI

object MCLogsAPI: AbstractAPI(
    api = "https://api.mclo.gs/1/log",
    root = "https://mclo.gs"
)