package com.cmc.launcher.components

import android.content.Context
import com.cmc.launcher.path.PathManager

class UnpackComponentsTask(context: Context, val component: Components) : UnpackSingleTask(
    context = context,
    rootDir = PathManager.DIR_COMPONENTS,
    assetsDirName = component.assetsDir,
    fileDirName = component.component
)