 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.ui.theme.festivals

import android.view.MotionEvent

/**
 * 点击彩蛋的输入观察器
 */
object FestivalTapObserver {
    private val listeners = ArrayList<(Float, Float) -> Unit>()

    /**
     * 订阅按下事件
     */
    fun subscribe(onTap: (x: Float, y: Float) -> Unit): () -> Unit {
        listeners.add(onTap)
        return { listeners.remove(onTap) }
    }

    fun observe(event: MotionEvent) {
        if (event.actionMasked != MotionEvent.ACTION_DOWN) return
        val x = event.x
        val y = event.y
        listeners.toList().forEach { listener ->
            listener(x, y)
        }
    }
}
