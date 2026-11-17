package com.cmc.launcher.game.renderer.config

import androidx.annotation.StringRes

@DslMarker
annotation class RendererConfigDsl

/** 以声明式 DSL 创建渲染器配置规范 */
fun rendererConfigSpec(block: RendererConfigSpecBuilder.() -> Unit): RendererConfigSpec =
    RendererConfigSpecBuilder().apply(block).build()

/** 收集固定环境变量和各类配置项 */
@RendererConfigDsl
class RendererConfigSpecBuilder {
    private val entries = mutableListOf<ConfigEntry>()
    private val fixedEnv = mutableMapOf<String, String>()

    fun fixedEnv(vararg values: Pair<String, String>) {
        fixedEnv += values
    }

    fun entry(entry: ConfigEntry) {
        entries += entry
    }

    fun switch(
        id: String,
        @StringRes title: Int,
        @StringRes description: Int? = null,
        default: Boolean,
        enabledValue: ConfigScalar = ConfigScalar.StringValue("1"),
        target: ConfigTarget
    ) = entry(
        SwitchEntry(
            id = id,
            title = ConfigText.Resource(title),
            description = description?.let(ConfigText::Resource),
            default = default,
            enabledValue = enabledValue,
            target = target
        )
    )

    fun choice(
        id: String,
        @StringRes title: Int,
        @StringRes description: Int? = null,
        default: Int,
        defaultEnabled: Boolean? = null,
        target: ConfigTarget = ConfigTarget.JsonField(id),
        options: RendererChoiceBuilder.() -> Unit
    ) {
        val optionList = RendererChoiceBuilder().apply(options).options
        require(optionList.isNotEmpty()) { "Choice entry $id must provide options" }
        val defaultValue = optionList.find { it.value.asString() == default.toString() }?.value
        requireNotNull(defaultValue) { "Choice entry $id does not contain its default value" }
        entry(
            SingleChoiceEntry(
                id = id,
                title = ConfigText.Resource(title),
                description = description?.let(ConfigText::Resource),
                options = optionList,
                defaultValue = defaultValue,
                defaultEnabled = defaultEnabled,
                target = target
            )
        )
    }

    fun orderedChoice(
        id: String,
        @StringRes title: Int,
        @StringRes description: Int? = null,
        default: List<String>,
        defaultEnabled: Boolean? = null,
        inheritFrom: String? = null,
        nativeValue: String? = null,
        target: ConfigTarget = ConfigTarget.JsonField(id),
        options: RendererChoiceBuilder.() -> Unit
    ) = orderedChoice(
        id = id,
        title = ConfigText.Resource(title),
        description = description?.let(ConfigText::Resource),
        default = default,
        defaultEnabled = defaultEnabled,
        inheritFrom = inheritFrom,
        nativeValue = nativeValue,
        target = target,
        options = options
    )

    fun orderedChoice(
        id: String,
        title: String,
        @StringRes description: Int? = null,
        default: List<String>,
        defaultEnabled: Boolean? = null,
        inheritFrom: String? = null,
        nativeValue: String? = null,
        target: ConfigTarget = ConfigTarget.JsonField(id),
        options: RendererChoiceBuilder.() -> Unit
    ) = orderedChoice(
        id = id,
        title = ConfigText.Dynamic(title),
        description = description?.let(ConfigText::Resource),
        default = default,
        defaultEnabled = defaultEnabled,
        inheritFrom = inheritFrom,
        nativeValue = nativeValue,
        target = target,
        options = options
    )

    private fun orderedChoice(
        id: String,
        title: ConfigText,
        description: ConfigText?,
        default: List<String>,
        defaultEnabled: Boolean?,
        inheritFrom: String?,
        nativeValue: String?,
        target: ConfigTarget,
        options: RendererChoiceBuilder.() -> Unit
    ) {
        val optionList = RendererChoiceBuilder().apply(options).options
        val optionKeys = optionList.map { it.value.asString() }
        require(optionList.isNotEmpty() && optionKeys.toSet().size == optionList.size) {
            "Ordered choice entry $id must provide unique options"
        }
        require(default.size == optionList.size && default.toSet() == optionKeys.toSet()) {
            "Ordered choice entry $id must provide every option in its default order"
        }
        if (inheritFrom != null) {
            require(inheritFrom != id && entries.any { it.id == inheritFrom }) {
                "Ordered choice entry $id must inherit from a preceding entry"
            }
            require(nativeValue in optionKeys) {
                "Ordered choice entry $id must provide a valid native value"
            }
        }
        entry(
            OrderedChoiceEntry(
                id = id,
                title = title,
                description = description,
                options = optionList,
                defaultValue = ConfigScalar.StringValue(default.joinToString(",")),
                defaultEnabled = defaultEnabled,
                target = target,
                inheritFrom = inheritFrom,
                nativeValue = nativeValue
            )
        )
    }

    fun number(
        id: String,
        @StringRes title: Int,
        @StringRes description: Int? = null,
        defaultValue: ConfigScalar.NumberValue,
        emptyValue: ConfigScalar.NumberValue? = null,
        integerOnly: Boolean = true,
        min: Double? = null,
        max: Double? = null,
        target: ConfigTarget
    ) = entry(
        NumberEntry(
            id = id,
            title = ConfigText.Resource(title),
            description = description?.let(ConfigText::Resource),
            defaultValue = defaultValue,
            emptyValue = emptyValue,
            integerOnly = integerOnly,
            min = min,
            max = max,
            target = target
        )
    )

    fun text(
        id: String,
        @StringRes title: Int,
        @StringRes description: Int? = null,
        default: String,
        target: ConfigTarget
    ) = entry(
        TextEntry(
            id = id,
            title = ConfigText.Resource(title),
            description = description?.let(ConfigText::Resource),
            default = default,
            target = target
        )
    )

    internal fun build(): RendererConfigSpec = RendererConfigSpec(entries, fixedEnv)
}

/** 收集单选配置项的候选值及其展示文案 */
@RendererConfigDsl
class RendererChoiceBuilder {
    internal val options = mutableListOf<ChoiceOption>()

    fun option(value: Int, @StringRes label: Int, @StringRes description: Int? = null) {
        option(
            value = ConfigScalar.IntValue(value),
            label = ConfigText.Resource(label),
            description = description?.let(ConfigText::Resource)
        )
    }

    fun option(value: String, @StringRes label: Int, @StringRes description: Int? = null) {
        option(
            value = ConfigScalar.StringValue(value),
            label = ConfigText.Resource(label),
            description = description?.let(ConfigText::Resource)
        )
    }

    fun option(value: String, label: String, description: String? = null) {
        option(
            value = ConfigScalar.StringValue(value),
            label = ConfigText.Dynamic(label),
            description = description?.let(ConfigText::Dynamic)
        )
    }

    fun option(value: Int, label: String, description: String? = null) {
        option(
            value = ConfigScalar.IntValue(value),
            label = ConfigText.Dynamic(label),
            description = description?.let(ConfigText::Dynamic)
        )
    }

    fun option(value: ConfigScalar, label: ConfigText? = null, description: ConfigText? = null) {
        options += ChoiceOption(value, label, description)
    }
}
