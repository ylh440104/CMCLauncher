package com.cmc.launcher.game.version.mod.meta

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class FabricModMetadata(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("version")
    val version: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("icon")
    val icon: String? = null,
    @SerializedName("authors")
    val authors: List<FabricModAuthor>? = null,
    @SerializedName("contact")
    val contact: Map<String, String>? = null
) {
    @JsonAdapter(FabricModAuthorSerializer::class)
    data class FabricModAuthor(
        @SerializedName("name")
        val name: String? = null
    )

    class FabricModAuthorSerializer : JsonSerializer<FabricModAuthor>,
        JsonDeserializer<FabricModAuthor> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): FabricModAuthor? {
            return if (json.isJsonPrimitive) {
                FabricModAuthor(json.asString)
            } else {
                FabricModAuthor(json.asJsonObject.getAsJsonPrimitive("name")?.asString)
            }
        }

        override fun serialize(
            src: FabricModAuthor?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement? {
            return src?.name?.let { JsonPrimitive(it) } ?: JsonNull.INSTANCE
        }
    }
}