 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.setting.unit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

abstract class AbstractSettingUnit<V>(
    val key: String,
    val defaultValue: V
) {
    /**
     * @return 获取当前的设置值
     */
    abstract fun getValue(): V

    /**
     * 保存设置值
     */
    protected abstract fun saveValue(v: V): V

    /**
     * 可观察的状态
     */
    var state by mutableStateOf(defaultValue)
        protected set

    fun init() {
        getValue()
    }

    /**
     * 保存当前状态值
     */
    fun save() {
        saveValue(this.state)
    }

    /**
     * 存入值
     */
    fun save(value: V) {
        this.state = saveValue(value)
    }

    /**
     * **仅更新状态**，不保存值
     */
    open fun updateState(value: V) {
        this.state = value
    }

    /**
     * 重置当前设置单元为默认值
     */
    fun reset() {
        this.state = saveValue(defaultValue)
    }
}