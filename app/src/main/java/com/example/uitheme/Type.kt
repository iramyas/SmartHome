package com.example.smarthome.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

val MyNearWhite = Color(0xFFFDFDFD)
val MySlightGray = Color(0xFFF1F1F1)
val MyCoolGrayLight = Color(0xFFE8EAED)
val MyMidGray = Color(0xFFB0B0B0)
val MyDarkGrayText = Color(0xFF212121)
val MyDarkGrayOutline = Color(0xFF424242)
val MyDarkBackground = Color(0xFF212121)
val MyDarkSurface = Color(0xFF303030)
val MyDarkOutline = Color(0xFF616161)
val MyLightGrayText = Color(0xFFE0E0E0)

val MyLightPrimaryPurple = Color(0xFFB39DDB)
val MyLightPrimaryContainerPurple = Color(0xFFE6E0F8)
val MyLightOnPrimaryPurple = Color(0xFF311B92)

val MyDarkPrimaryBlue = Color(0xFF80DEEA)
val MyDarkPrimaryContainerBlue = Color(0xFF004D5F)
val MyDarkOnPrimaryBlue = Color(0xFF001F2A)

val LightGrayishPink = Color(0xFFF0E0E0)
val SoftBubblegumPink = Color(0xFFF8A5C2)

private val MyLightColorScheme = lightColorScheme(
    primary = MyLightPrimaryPurple,
    onPrimary = MyDarkGrayText,
    primaryContainer = MyLightPrimaryContainerPurple,
    onPrimaryContainer = MyDarkGrayText,
    secondary = MyMidGray,
    onSecondary = MyDarkGrayText,
    secondaryContainer = MySlightGray,
    onSecondaryContainer = MyDarkGrayText,
    tertiary = MyMidGray,
    onTertiary = MyDarkGrayText,
    tertiaryContainer = MySlightGray,
    onTertiaryContainer = MyDarkGrayText,
    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFCD8DF),
    onErrorContainer = Color(0xFFB00020),
    background = MyNearWhite,
    onBackground = MyDarkGrayText,
    surface = MySlightGray,
    onSurface = MyDarkGrayText,
    surfaceVariant = MyCoolGrayLight,
    onSurfaceVariant = MyDarkGrayText,
    outline = MyDarkGrayOutline,
    outlineVariant = MyMidGray,
    inverseSurface = MyDarkBackground,
    inverseOnSurface = MyLightGrayText,
    inversePrimary = MyDarkPrimaryBlue,
    surfaceTint = MyLightPrimaryPurple,
    scrim = Color(0x99000000),
)

private val MyDarkColorScheme = darkColorScheme(
    primary = MyDarkPrimaryBlue,
    onPrimary = MyDarkOnPrimaryBlue,
    primaryContainer = MyDarkPrimaryContainerBlue,
    onPrimaryContainer = MyLightGrayText,
    secondary = Color(0xFF82B1FF),
    onSecondary = MyDarkOnPrimaryBlue,
    secondaryContainer = Color(0xFF004A7F),
    onSecondaryContainer = MyLightGrayText,
    tertiary = Color(0xFFA0CAFF),
    onTertiary = MyDarkOnPrimaryBlue,
    tertiaryContainer = Color(0xFF004A7F),
    onTertiaryContainer = MyLightGrayText,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFB4AB),
    background = MyDarkBackground,
    onBackground = MyLightGrayText,
    surface = MyDarkSurface,
    onSurface = MyLightGrayText,
    surfaceVariant = Color(0xFF424242),
    onSurfaceVariant = MyMidGray,
    outline = MyDarkOutline,
    outlineVariant = Color(0xFF525252),
    inverseSurface = MyNearWhite,
    inverseOnSurface = MyDarkGrayText,
    inversePrimary = MyLightPrimaryPurple,
    surfaceTint = MyDarkPrimaryBlue,
    scrim = Color(0xB3000000),
)

private val MyPinkColorScheme = lightColorScheme(
    primary = SoftBubblegumPink,
    onPrimary = MyDarkGrayText,
    primaryContainer = LightGrayishPink,
    onPrimaryContainer = MyDarkGrayText,
    secondary = MyCoolGrayLight,
    onSecondary = MyDarkGrayText,
    secondaryContainer = MySlightGray,
    onSecondaryContainer = MyDarkGrayText,
    tertiary = MyMidGray,
    onTertiary = MyDarkGrayText,
    tertiaryContainer = MySlightGray,
    onTertiaryContainer = MyDarkGrayText,
    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFCD8DF),
    onErrorContainer = Color(0xFFB00020),
    background = MyNearWhite,
    onBackground = MyDarkGrayText,
    surface = MyNearWhite,
    onSurface = MyDarkGrayText,
    surfaceVariant = LightGrayishPink,
    onSurfaceVariant = MyDarkGrayText,
    outline = MyCoolGrayLight,
    outlineVariant = MySlightGray,
    inverseSurface = MyDarkBackground,
    inverseOnSurface = MyLightGrayText,
    inversePrimary = MyDarkPrimaryBlue,
    surfaceTint = SoftBubblegumPink,
    scrim = Color(0x99000000),
)

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    )
)

enum class ThemeOption {
    Light, Dark, Pink, System
}

@Composable
fun SmartHomeTheme(
    selectedTheme: ThemeOption = ThemeOption.Light,
    dynamicColor: Boolean = false,
    content: @Composable (() -> Unit)
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        selectedTheme == ThemeOption.Dark -> MyDarkColorScheme
        selectedTheme == ThemeOption.Pink -> MyPinkColorScheme
        else -> MyLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    selectedTheme == ThemeOption.Light || selectedTheme == ThemeOption.Pink
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
