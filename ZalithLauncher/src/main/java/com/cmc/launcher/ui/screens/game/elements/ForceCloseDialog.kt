 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui.screens.game.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cmc.launcher.R
import com.cmc.launcher.ui.components.SimpleAlertDialog

sealed interface ForceCloseOperation {
    data object None : ForceCloseOperation
    /** 显示强制关闭对话框 */
    data object Show : ForceCloseOperation
}

@Composable
fun ForceCloseOperation(
    operation: ForceCloseOperation,
    onChange: (ForceCloseOperation) -> Unit,
    onForceClose: () -> Unit,
    text: String
) {
    when (operation) {
        ForceCloseOperation.None -> {}
        ForceCloseOperation.Show -> {
            SimpleAlertDialog(
                title = stringResource(R.string.game_button_force_close),
                text = text,
                onConfirm = onForceClose,
                onDismiss = {
                    onChange(ForceCloseOperation.None)
                }
            )
        }
    }
}