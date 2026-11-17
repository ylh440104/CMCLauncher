package com.cmc.launcher.crashlogs

import com.cmc.launcher.path.GLOBAL_CLIENT
import com.cmc.launcher.utils.network.safeBodyAsJson
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Parameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class AbstractAPI(
    val api: String,
    val root: String
) {
    /**
     * 上传日志到目标服务器，并生成链接
     * @return 返回生成的链接
     */
    @Throws(Exception::class)
    suspend fun onUpload(content: String): MCLogsResponse {
        return withContext(Dispatchers.IO) {
            val response = GLOBAL_CLIENT.post(urlString = api) {
                setBody(
                    body = FormDataContent(
                        formData = Parameters.build {
                            append("content", content)
                        }
                    )
                )
            }

            response.safeBodyAsJson<MCLogsResponse>()
        }
    }
}