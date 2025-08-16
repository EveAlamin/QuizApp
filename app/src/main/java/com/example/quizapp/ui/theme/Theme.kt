package com.example.quizapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = DarkGreen,
    onPrimary = White,
    primaryContainer = LightGreen,
    onPrimaryContainer = DarkGreen,
    secondary = LightGreen,
    onSecondary = DarkGreen,
    background = White,
    onBackground = DarkText,
    surface = OffWhite,
    onSurface = DarkText,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = DarkText,
    error = ErrorRed,
    errorContainer = LightErrorRed,
    onErrorContainer = ErrorRed
)

@Composable
fun QuizAppTheme(
    darkTheme: Boolean = false, // Padrão agora é o tema claro
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}