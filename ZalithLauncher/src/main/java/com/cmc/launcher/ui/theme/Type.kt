package com.cmc.launcher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Display = TextStyle(
    fontWeight = FontWeight.Black,
    fontSize = 42.sp,
    lineHeight = 46.sp,
    letterSpacing = (-0.8).sp
)

private val Headline = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 26.sp,
    lineHeight = 32.sp,
    letterSpacing = (-0.4).sp
)

private val Title = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    lineHeight = 21.sp,
    letterSpacing = (-0.1).sp
)

private val Body = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.sp
)

private val Label = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 15.sp,
    letterSpacing = 0.2.sp
)

private val Micro = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp,
    lineHeight = 13.sp,
    letterSpacing = 0.4.sp
)

val AppTypography = Typography(
    displayLarge = Display,
    displayMedium = Display.copy(fontSize = 34.sp, lineHeight = 38.sp),
    displaySmall = Headline.copy(fontSize = 30.sp, lineHeight = 36.sp),
    headlineLarge = Headline,
    headlineMedium = Headline.copy(fontSize = 22.sp, lineHeight = 28.sp),
    headlineSmall = Title.copy(fontSize = 20.sp, lineHeight = 26.sp),
    titleLarge = Title.copy(fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = Title,
    titleSmall = Title.copy(fontSize = 14.sp, lineHeight = 19.sp),
    bodyLarge = Body.copy(fontSize = 15.sp, lineHeight = 21.sp),
    bodyMedium = Body,
    bodySmall = Body.copy(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = Label.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = Label,
    labelSmall = Micro
)