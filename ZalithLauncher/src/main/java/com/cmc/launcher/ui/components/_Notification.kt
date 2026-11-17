 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.cmc.launcher.R
import com.cmc.launcher.notification.NotificationManager

/**
 * 通知权限检查
 * @param onGranted 用户授予了权限
 * @param onIgnore 用户忽略了权限申请（拒绝）
 * @param onDismiss 用户关闭了权限申请弹窗
 */
@Composable
fun NotificationCheck(
    title: String = stringResource(R.string.notification_title),
    text: String,
    onGranted: () -> Unit = {},
    onIgnore: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                onGranted()
            } else {
                onIgnore()
            }
        }

    SimpleAlertDialog(
        title = title,
        text = { Text(text = text) },
        confirmText = stringResource(R.string.notification_request),
        dismissText = stringResource(R.string.generic_ignore),
        onConfirm = {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                //13- 跳转至设置，让用户自行开启通知权限
                NotificationManager.openNotificationSettings(context)
                onDismiss()
            } else {
                //安卓 13+ 可以直接弹出通知权限申请
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        onCancel = {
            onIgnore()
        },
        onDismissRequest = {
            onDismiss()
        }
    )
}