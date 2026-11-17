package com.cmc.launcher.database

import androidx.room.TypeConverter
import com.cmc.launcher.game.account.wardrobe.SkinModelType

class Converters {
    @TypeConverter
    fun fromSkinModelType(type: SkinModelType): String = type.name

    @TypeConverter
    fun toSkinModelType(value: String): SkinModelType =
        enumValueOf(value)
}