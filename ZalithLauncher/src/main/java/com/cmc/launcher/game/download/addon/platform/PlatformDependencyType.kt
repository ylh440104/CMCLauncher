 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.cmc.launcher.game.download.addon.platform

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = PlatformDependencyType.Serializer::class)
enum class PlatformDependencyType(val curseforgeCode: Int) {
    @SerialName("required")
    REQUIRED(3),            //依赖
    @SerialName("optional")
    OPTIONAL(2),            //可选
    @SerialName("incompatible")
    INCOMPATIBLE(5),        //不兼容
    @SerialName("embedded")
    EMBEDDED(1),            //嵌入式

    @SerialName("tool")
    TOOL(4),                //工具 (CurseForge)
    @SerialName("include")
    INCLUDE(6);             //包括 (CurseForge)

    companion object {
        private val map = PlatformDependencyType.entries.associateBy { it.curseforgeCode }
        fun fromCurseForgeCode(code: Int): PlatformDependencyType = map[code] ?: error("Unknown dependency code: $code")
    }

    object Serializer : KSerializer<PlatformDependencyType> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("PlatformDependencyType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): PlatformDependencyType {
            return when (val input = runCatching { decoder.decodeInt() }.getOrNull()) {
                null -> {
                    val name = decoder.decodeString().lowercase()
                    when (name) {
                        "required" -> REQUIRED
                        "optional" -> OPTIONAL
                        "incompatible" -> INCOMPATIBLE
                        "embedded" -> EMBEDDED
                        else -> error("Unknown type name: $name")
                    }
                }
                else -> {
                    PlatformDependencyType.fromCurseForgeCode(input)
                }
            }
        }

        override fun serialize(encoder: Encoder, value: PlatformDependencyType) {
            val name = when (value) {
                REQUIRED -> "required"
                OPTIONAL -> "optional"
                INCOMPATIBLE -> "incompatible"
                EMBEDDED -> "embedded"
                TOOL -> "tool"
                INCLUDE -> "include"
            }
            encoder.encodeString(name)
        }
    }
}