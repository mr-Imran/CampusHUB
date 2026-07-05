package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Bold Typography definition
val Typography =
  Typography(
    displayLarge = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Black,
      fontSize = 40.sp,
      lineHeight = 44.sp,
      letterSpacing = (-1.5).sp
    ),
    displayMedium = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Black,
      fontSize = 32.sp,
      lineHeight = 36.sp,
      letterSpacing = (-1.0).sp
    ),
    headlineLarge = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Black,
      fontSize = 28.sp,
      lineHeight = 32.sp,
      letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 22.sp,
      lineHeight = 26.sp,
      letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 18.sp,
      lineHeight = 22.sp,
      letterSpacing = (-0.25).sp
    ),
    titleMedium = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Bold,
      fontSize = 16.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 24.sp,
      letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Bold,
      fontSize = 12.sp,
      lineHeight = 16.sp,
      letterSpacing = 1.sp
    ),
    labelSmall = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Black,
      fontSize = 10.sp,
      lineHeight = 12.sp,
      letterSpacing = 1.5.sp
    )
  )
