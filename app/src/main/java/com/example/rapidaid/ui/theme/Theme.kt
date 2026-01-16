package com.example.rapidaid.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PurplePrimary = Color(0xFF6A1B9A)
private val PurpleSecondary = Color(0xFF9C27B0)
private val PurpleTertiary = Color(0xFFE1BEE7)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    secondary = PurpleSecondary,
    tertiary = PurpleTertiary,

    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,

    outline = PurplePrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    secondary = PurpleSecondary,
    tertiary = PurpleTertiary
)

@Composable
fun RapidAidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        window.statusBarColor = colorScheme.primary.value.toInt()
        WindowCompat.getInsetsController(window, view)
            .isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )

}
