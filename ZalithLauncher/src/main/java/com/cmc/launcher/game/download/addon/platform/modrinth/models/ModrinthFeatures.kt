package com.cmc.launcher.game.download.addon.platform.modrinth.models

import com.cmc.launcher.R
import com.cmc.launcher.game.download.addon.platform.PlatformFilterCode

enum class ModrinthFeatures : ModrinthFacet, PlatformFilterCode {
    AUDIO {
        override fun facetValue(): String = "audio"
        override fun getDisplayName(): Int = R.string.download_addon_features_audio
    },
    BLOCKS {
        override fun facetValue(): String = "blocks"
        override fun getDisplayName(): Int = R.string.download_addon_features_blocks
    },
    CORE_SHADERS {
        override fun facetValue(): String = "core-shaders"
        override fun getDisplayName(): Int = R.string.download_addon_features_core_shaders
    },
    ENTITIES {
        override fun facetValue(): String = "entities"
        override fun getDisplayName(): Int = R.string.download_addon_features_entities
    },
    ENVIRONMENT {
        override fun facetValue(): String = "environment"
        override fun getDisplayName(): Int = R.string.download_addon_features_environment
    },
    FONTS {
        override fun facetValue(): String = "fonts"
        override fun getDisplayName(): Int = R.string.download_addon_features_fonts
    },
    GUI {
        override fun facetValue(): String = "gui"
        override fun getDisplayName(): Int = R.string.download_addon_features_gui
    },
    ITEMS {
        override fun facetValue(): String = "items"
        override fun getDisplayName(): Int = R.string.download_addon_features_items
    },
    LOCALE {
        override fun facetValue(): String = "locale"
        override fun getDisplayName(): Int = R.string.download_addon_features_locale
    },
    MODELS {
        override fun facetValue(): String = "models"
        override fun getDisplayName(): Int = R.string.download_addon_features_models
    };

    override fun facetName(): String = "features"
    override fun index(): Int = this.ordinal
}