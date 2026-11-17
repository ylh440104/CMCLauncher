 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

@file:Suppress("unused")

/**
 * 部分实现思路参考自 [G-Mapper for Android](https://github.com/Mathias-Boulay/android_gamepad_remapper/)，
 * 该项目采用 GNU Lesser General Public License v3.0（LGPL-3.0）授权。
 *
 *
 * 说明：
 * - 启动器使用 Jetpack Compose 构建 UI，无法直接集成原库，
 *   因此参考其核心逻辑并以 Kotlin 与 Compose 风格重新实现。
 * - 原项目的部分算法与结构在保留设计意图的前提下进行了简化与重构。
 * - 本项目未直接包含或链接原项目的源代码。
 */

package com.cmc.launcher.ui.control.gamepad
