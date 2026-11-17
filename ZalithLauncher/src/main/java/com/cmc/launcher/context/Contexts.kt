 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.context

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.annotation.RawRes
import com.cmc.launcher.path.PathManager
import com.cmc.launcher.utils.file.ensureParentDirectory
import com.cmc.launcher.utils.file.readString
import com.cmc.launcher.utils.logging.Logger
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.properties.Delegates

private const val TAG = "Contexts"

var GlobalContext by Delegates.notNull<Context>()

fun refreshContext(context: Context) {
    PathManager.refreshPaths(context)
}

fun Context.readAssetFile(filePath: String): String {
    return assets.open(filePath).use { it.readString() }
}

fun Context.getFileName(uri: Uri): String? {
    return runCatching {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null

            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it != -1 }?.let { columnIndex ->
                cursor.getString(columnIndex)
            }
        } ?: uri.lastPathSegment
    }.getOrNull() ?: uri.lastPathSegment
}

@Throws(IOException::class)
fun Context.copyAssetFile(fileName: String, output: String, overwrite: Boolean) {
    this.copyAssetFile(fileName, output, File(fileName).name, overwrite)
}

@Throws(IOException::class)
fun Context.copyAssetFile(
    fileName: String,
    output: String,
    outputName: String,
    overwrite: Boolean
) {
    this.copyAssetFile(fileName, File(output, outputName), overwrite)
}

@Throws(IOException::class)
fun Context.copyAssetFile(
    fileName: String,
    output: File,
    overwrite: Boolean
) {
    val destinationFile = output.ensureParentDirectory()
    if (destinationFile.exists() && !overwrite) return
    assets.open(fileName).use { input ->
        FileOutputStream(output).use { out ->
            input.copyTo(out)
            out.flush()
            out.fd.sync()
        }
    }
}

@Throws(IOException::class)
fun Context.copyLocalFile(
    uri: Uri,
    outputFile: File
) {
    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    try {
        contentResolver.takePersistableUriPermission(uri, flags)
    } catch (e: SecurityException) {
        Logger.warning(TAG, "Failed to take persistable permission for URI: $uri", e)
    }

    if (outputFile.parentFile?.exists() != true && outputFile.parentFile?.mkdirs() != true) {
        Logger.warning(TAG, "Failed to create parent directories for output file.")
    }
    if (!outputFile.exists() && !outputFile.createNewFile()) {
        Logger.warning(TAG, "Unable to manually create file when importing from URI to local storage.")
    }
    contentResolver.openInputStream(uri).use { inputStream ->
        FileUtils.copyToFile(inputStream, outputFile)
    }
}

@Throws(IOException::class)
fun Context.writeLocalFile(
    inputFile: File,
    outputUri: Uri,
    mimeType: String
) {
    val baseDocId = DocumentsContract.getTreeDocumentId(outputUri)
    val fileUri =
        DocumentsContract.buildDocumentUriUsingTree(outputUri, "$baseDocId/${inputFile.name}")

    try {
        contentResolver.openOutputStream(fileUri, "wt")?.use { out ->
            FileUtils.copyFile(inputFile, out)
            return
        }
    } catch (_: IOException) {
        // handle below
    } catch (_: RuntimeException) {
        // handle below
    }

    val parentDocumentUri = DocumentsContract.buildDocumentUriUsingTree(
        outputUri,
        DocumentsContract.getTreeDocumentId(outputUri)
    )

    val newFileUri =
        DocumentsContract.createDocument(contentResolver, parentDocumentUri, mimeType, inputFile.name)
            ?: throw IOException("Failed to create document: ${inputFile.name}")

    contentResolver.openOutputStream(newFileUri, "wt")?.use { out ->
        FileUtils.copyFile(inputFile, out)
    }
}

fun Context.readRawContent(
    @RawRes raw: Int
): String {
    return resources.openRawResource(raw)
        .bufferedReader()
        .readText()
}