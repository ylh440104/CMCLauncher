 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.coroutine

import kotlinx.coroutines.CompletableDeferred

class DataBridge<T> {
    private val deferred = CompletableDeferred<T>()

    suspend fun awaitData(): T {
        return deferred.await()
    }

    fun provideData(value: T) {
        if (!deferred.isCompleted) {
            deferred.complete(value)
        }
    }
}