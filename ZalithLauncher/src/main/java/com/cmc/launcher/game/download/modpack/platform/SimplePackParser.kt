package com.cmc.launcher.game.download.modpack.platform

import com.cmc.launcher.utils.GSON
import com.cmc.launcher.utils.file.locateRealRoot
import com.cmc.launcher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "SimplePackParser"

/**
 * 较为简单的整合包解析器，适合结构较为简单的，以索引/清单文件为特征的整合包，
 * 可以统一代码，共用此解析逻辑
 * @param extraProcess 如果识别成功，则开始额外的逻辑处理，用于更加严谨的判断，`true` 则代表确认为该格式
 */
abstract class SimplePackParser<E: PackManifest>(
    val indexFilePath: String,
    protected val manifestClass: Class<E>,
    private val extraProcess: (suspend (rootFolder: File) -> Boolean)? = null,
    private val buildPack: (rootFolder: File, manifest: E) -> AbstractPack
) : PackParser {

    override suspend fun parse(packFolder: File): AbstractPack? {
        val root = locateRealRoot(packFolder)

        //整合包索引文件
        val indexFile = File(root, indexFilePath)
        return withContext(Dispatchers.IO) {
            if (!indexFile.exists()) {
                Logger.debug(TAG, "${getIdentifier()} parser -> manifest file does not exist $indexFile")
                return@withContext null
            }

            //尝试读取并识别，如果识别成功，则判断其为该格式的整合包
            val rawString = indexFile.readText()
            val manifest = GSON.fromJson(rawString, manifestClass)

            //识别成功，开始额外的逻辑处理
            if (extraProcess?.invoke(root) == false) {
                //判断失败了，排除这个格式
                return@withContext null
            }

            buildPack(root, manifest)
        }
    }
}