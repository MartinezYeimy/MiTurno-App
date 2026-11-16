package com.miturno.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Paleta de Mi Turno: azul principal, azul de botones, azul muy claro, blanco y azul oscuro para textos
val BluePrimary = Color(0xFF4DA3FF)
val Blue700 = Color(0xFF2E7DD1)
val Blue900 = Color(0xFF17365D)
val Blue100 = Color(0xFFD6E8FC)
val Blue50 = Color(0xFFEAF4FF)
val Ink = Color(0xFF17365D)
val InkSoft = Color(0xFF3B5578)
val OutlineBlue = Color(0xFFA9C8EA)
val DangerRed = Color(0xFFC0392B)
val DangerBg = Color(0xFFFDE4E1)
val SuccessGreen = Color(0xFF14532D)
val SuccessBg = Color(0xFFDDF3E4)
val NeutralBg = Color(0xFFE8EEF5)

private val Scheme = lightColorScheme(
    primary = Blue700,
    onPrimary = Color.White,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue900,
    secondary = BluePrimary,
    onSecondary = Color.White,
    secondaryContainer = Blue100,
    onSecondaryContainer = Blue900,
    background = Color.White,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Blue50,
    onSurfaceVariant = InkSoft,
    outline = OutlineBlue,
    outlineVariant = Blue100,
    error = DangerRed,
    onError = Color.White,
    inverseSurface = Blue900,
    inverseOnSurface = Color.White
)

// Tamaños grandes pensados para adultos mayores (todo en sp: respeta el tamaño de letra del sistema)
private val AppTypography = Typography(
    displayLarge = TextStyle(fontSize = 68.sp, lineHeight = 72.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 20.sp, lineHeight = 28.sp),
    bodyMedium = TextStyle(fontSize = 18.sp, lineHeight = 26.sp),
    bodySmall = TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
    labelLarge = TextStyle(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun MiTurnoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Scheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
