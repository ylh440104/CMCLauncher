package com.cmc.launcher.utils

fun Boolean.getInt(): Int = if (this) 1 else 0

fun Int.toBoolean(): Boolean = this != 0