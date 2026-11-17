package com.cmc.launcher.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.runtime.serialization.NavKeySerializer
import com.cmc.launcher.ui.AndroidStringText

interface TitledNavKey: NavKey {
    val title: AndroidStringText?
        get() = null
}

@Composable
fun rememberTitledNavBackStack(vararg elements: TitledNavKey): NavBackStack<TitledNavKey> {
    return rememberSerializable(
        serializer = NavBackStackSerializer(elementSerializer = NavKeySerializer())
    ) {
        NavBackStack(*elements)
    }
}