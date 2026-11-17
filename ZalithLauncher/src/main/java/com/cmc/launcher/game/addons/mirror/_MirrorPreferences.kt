 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.addons.mirror

import com.cmc.launcher.setting.enums.MirrorSourceType

/** 设置折算后的镜像使用策略 */
enum class MirrorPriority {
    /** 只使用官方源 */
    OFFICIAL,
    /** 镜像在前、官方在后 */
    MIRROR_FIRST
}

/**
 * 把三档设置折算成镜像使用策略
 * 自动档依据是否中国大陆静态判定，失败换源由下载引擎在运行时自适应，无需网络探测。
 */
fun resolveMirrorPriority(source: MirrorSourceType, mainland: Boolean): MirrorPriority =
    when {
        source == MirrorSourceType.OFFICIAL -> MirrorPriority.OFFICIAL
        source == MirrorSourceType.MIRROR -> MirrorPriority.MIRROR_FIRST
        mainland -> MirrorPriority.MIRROR_FIRST
        else -> MirrorPriority.OFFICIAL
    }

/** 按生效策略产出候选列表；官方档不注入镜像，镜像链接不存在时仅保留官方源 */
fun orderCandidates(official: String, mirror: String?, priority: MirrorPriority): List<String> =
    when (priority) {
        MirrorPriority.OFFICIAL -> listOfNotNull(official)
        MirrorPriority.MIRROR_FIRST -> listOfNotNull(mirror, official)
    }
