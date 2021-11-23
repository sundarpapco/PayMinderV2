package com.example.payminder.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = GrayishBlue,
    primaryVariant = GrayishBlue,
    secondary = SkyBlue,
    secondaryVariant = SkyBlue,
    surface = GrayishBlue,
    background = NightBlue,
    error= ErrorRed,
    onPrimary = Color.White,
    onSecondary=Color.White,
    onSurface = Color.White,
    onError = Color.White,
    onBackground = Color.White

)

private val LightColorPalette = lightColors(
    primary = GrayishBlue,
    primaryVariant = GrayishBlue,
    secondary = SkyBlue,
    secondaryVariant = SkyBlue,
    surface = GrayishBlue,
    background = NightBlue,
    error= ErrorRed,
    onPrimary = Color.White,
    onSecondary=Color.White,
    onSurface = Color.White,
    onError = Color.White,
    onBackground = Color.White
)

@Composable
fun PayMinderTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}