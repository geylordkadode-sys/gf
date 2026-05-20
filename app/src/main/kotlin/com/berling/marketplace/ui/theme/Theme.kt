package com.berling.marketplace.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PrimaryPink = Color(0xFFE91E63)
private val PrimaryDarkPink = Color(0xFFC2185B)
private val LightPink = Color(0xFFFCE4EC)
private val BackgroundLight = Color(0xFFFAF9F8)
private val BackgroundDark = Color(0xFF121212)
private val TextDark = Color(0xFF2C2C2C)
private val TextLight = Color(0xFFFFFFFF)

private val LightColors = lightColorScheme(
    primary = PrimaryPink,
    onPrimary = TextLight,
    primaryContainer = LightPink,
    onPrimaryContainer = PrimaryDarkPink,
    secondary = PrimaryDarkPink,
    onSecondary = TextLight,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = TextLight,
    onSurface = TextDark,
    error = Color(0xFFB3261E),
    onError = TextLight
)

private val DarkColors = darkColorScheme(
    primary = PrimaryPink,
    onPrimary = TextDark,
    primaryContainer = PrimaryDarkPink,
    onPrimaryContainer = LightPink,
    secondary = PrimaryDarkPink,
    onSecondary = TextDark,
    background = BackgroundDark,
    onBackground = TextLight,
    surface = Color(0xFF1E1E1E),
    onSurface = TextLight,
    error = Color(0xFFF2B8B5),
    onError = TextDark
)

@Composable
fun BerlingMarketplaceTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
