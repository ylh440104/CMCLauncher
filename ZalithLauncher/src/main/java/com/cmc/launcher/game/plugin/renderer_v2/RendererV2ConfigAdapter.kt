package com.cmc.launcher.game.plugin.renderer_v2

import com.cmc.launcher.game.plugin.renderer_v2.data.RendererConfig
import com.cmc.launcher.game.renderer.config.ChoiceOption
import com.cmc.launcher.game.renderer.config.ConfigScalar
import com.cmc.launcher.game.renderer.config.ConfigTarget
import com.cmc.launcher.game.renderer.config.ConfigText
import com.cmc.launcher.game.renderer.config.RendererConfigSpec
import com.cmc.launcher.game.renderer.config.SingleChoiceEntry
import com.cmc.launcher.game.renderer.config.SwitchEntry
import com.cmc.launcher.game.renderer.config.TextEntry

/**
 * 将 V2 插件格式转换为中立配置规范
 */
fun RendererConfig.toConfigSpec(genText: (String) -> String?): RendererConfigSpec {
    val fixedEnv = env.filterIsInstance<RendererConfig.Env.NormalEnv>()
        .associate { it.key to it.value }

    val entries = env.mapNotNull { entry ->
        when (entry) {
            is RendererConfig.Env.NormalEnv -> null
            is RendererConfig.Env.ToggleableEnv -> SwitchEntry(
                id = entry.key,
                title = entry.title.dynamicText(genText),
                description = null,
                default = entry.toggle,
                enabledValue = ConfigScalar.StringValue(entry.value),
                target = ConfigTarget.Environment(entry.key)
            )
            is RendererConfig.Env.SelectableEnv -> {
                val values = buildList {
                    add(entry.items.defaultValue)
                    addAll(entry.items.values)
                }.distinct()
                SingleChoiceEntry(
                    id = entry.key,
                    title = entry.title.dynamicText(genText),
                    description = null,
                    options = values.map { ChoiceOption(ConfigScalar.StringValue(it)) },
                    defaultValue = ConfigScalar.StringValue(entry.items.defaultValue),
                    defaultEnabled = entry.check,
                    target = ConfigTarget.Environment(entry.key)
                )
            }
            is RendererConfig.Env.CustomizableEnv -> TextEntry(
                id = entry.key,
                title = entry.title.dynamicText(genText),
                description = null,
                default = entry.defaultValue.orEmpty(),
                target = ConfigTarget.Environment(entry.key)
            )
        }
    }
    return RendererConfigSpec(entries = entries, fixedEnv = fixedEnv)
}

private fun RendererConfig.MetaString?.dynamicText(genText: (String) -> String?): ConfigText? =
    this?.key?.let(genText)?.let(ConfigText::Dynamic)
