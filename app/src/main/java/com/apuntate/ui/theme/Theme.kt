package com.apuntate.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de grises: acento gris un poco oscuro para pestañas y botones.
private val GrisAccent = Color(0xFF4A5568)      // gris un poco oscuro (botones, pestañas)
private val GrisAccentClaro = Color(0xFF718096)  // gris medio (secundario)
private val GrisContenedor = Color(0xFFE2E8F0)   // gris muy claro (contenedores)
private val Verde = Color(0xFF2E7D32)

private val LightColors = lightColorScheme(
    primary = GrisAccent,
    onPrimary = Color.White,
    secondary = GrisAccentClaro,
    onSecondary = Color.White,
    primaryContainer = GrisContenedor,
    onPrimaryContainer = Color(0xFF1A202C),
    secondaryContainer = GrisContenedor,
    onSecondaryContainer = Color(0xFF1A202C),
    tertiary = Verde,
    background = Color(0xFFF7F8FA),
    surface = Color(0xFFFFFFFF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA0AEC0),
    onPrimary = Color(0xFF1A202C),
    secondary = Color(0xFF718096),
    primaryContainer = Color(0xFF2D3748),
    onPrimaryContainer = Color(0xFFE2E8F0),
    secondaryContainer = Color(0xFF2D3748),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = Verde
)

@Composable
fun ApuntateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Usamos nuestra paleta de grises (sin dynamic color) para un aspecto consistente.
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
