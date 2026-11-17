 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.utils.animation

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween

/**
 * 获取通用动画的持续时长
 */
private fun getAnimateSpeed(): Int = SwapTimeline.time(SwapTimeline.CONTENT_BASE)

fun <E> getAnimateTween(
    delayMillis: Int = 0
): FiniteAnimationSpec<E> = getAnimateTween(
    durationMillis = getAnimateSpeed(),
    delayMillis = delayMillis
)

fun <E> getAnimateTween(
    durationMillis: Int,
    delayMillis: Int = 0
): FiniteAnimationSpec<E> = tween(
    durationMillis = durationMillis,
    delayMillis = delayMillis
)

fun <E> getEnterTween(
    delayMillis: Int = 0
): FiniteAnimationSpec<E> = tween(
    durationMillis = getAnimateSpeed(),
    delayMillis = delayMillis,
    easing = EnterTranslateEasing
)