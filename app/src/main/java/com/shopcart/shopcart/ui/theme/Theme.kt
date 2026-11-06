package com.shopcart.shopcart.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val Green = Color(0xFF1B6B4A)
private val GreenLight = Color(0xFF7FD6A8)
private val Amber = Color(0xFFE38B00)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEBD9),
    onPrimaryContainer = Color(0xFF00210F),
    secondary = Color(0xFF4E6355),
    secondaryContainer = Color(0xFFD1E8D6),
    tertiary = Amber,
    onTertiary = Color.White,
    background = Color(0xFFF6F7F5),
    surface = Color(0xFFF6F7F5),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF1F3F0),
    surfaceContainer = Color(0xFFEBEEEA),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = GreenLight,
    onPrimary = Color(0xFF00391F),
    primaryContainer = Color(0xFF005234),
    onPrimaryContainer = Color(0xFFCDEBD9),
    secondary = Color(0xFFB5CCBA),
    tertiary = Color(0xFFFFB866),
    background = Color(0xFF121412),
    surface = Color(0xFF121412),
    surfaceContainerLowest = Color(0xFF0D0F0D),
    surfaceContainerLow = Color(0xFF1A1C1A),
    surfaceContainer = Color(0xFF1E211E),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
)

@Composable
fun ShopCartTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = AppShapes,
        content = content,
    )
}
