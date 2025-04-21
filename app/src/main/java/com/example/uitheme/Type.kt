package com.example.smarthome.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.* // Keep wildcard import
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

// --- Your Custom Color Definitions (Keep as is) ---
// Neutrals
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
// Light Theme Accent
val MyLightPrimaryPurple = Color(0xFFB39DDB)
val MyLightPrimaryContainerPurple = Color(0xFFE6E0F8)
val MyLightOnPrimaryPurple = Color(0xFF311B92)
// Dark Theme Accent
val MyDarkPrimaryBlue = Color(0xFF80DEEA)
val MyDarkPrimaryContainerBlue = Color(0xFF004D5F)
val MyDarkOnPrimaryBlue = Color(0xFF001F2A)
// Pink Theme Accent
val MyLightPink = Color(0xFFFCE4EC)
val MyMidPink = Color(0xFFF48FB1)
val MyDarkPink = Color(0xFFAD1457)

// --- Your Color Schemes (Keep as is) ---
// 1. Light Theme
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
// 2. Dark Theme
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
// 3. Pink Theme
private val MyPinkColorScheme = lightColorScheme(
    primary = MyMidPink,
    onPrimary = Color.White,
    primaryContainer = MyLightPink,
    onPrimaryContainer = MyDarkPink,
    secondary = MyCoolGrayLight,
    onSecondary = MyDarkGrayText,
    secondaryContainer = Color(0xFFF5F5F5),
    onSecondaryContainer = MyDarkGrayText,
    tertiary = MyDarkPink,
    onTertiary = Color.White,
    tertiaryContainer = MyLightPink,
    onTertiaryContainer = MyDarkPink,
    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFCD8DF),
    onErrorContainer = Color(0xFFB00020),
    background = MyCoolGrayLight,
    onBackground = MyDarkGrayText,
    surface = MyNearWhite,
    onSurface = MyDarkGrayText,
    surfaceVariant = MyLightPink,
    onSurfaceVariant = MyDarkPink,
    outline = MyDarkPink,
    outlineVariant = MyMidPink,
    inverseSurface = MyDarkBackground,
    inverseOnSurface = MyLightGrayText,
    inversePrimary = MyDarkPrimaryBlue,
    surfaceTint = MyMidPink,
    scrim = Color(0x99000000),
    // Example in ui/theme/Theme.kt (adjust colors) // Or darkColorScheme if pink is dark
//    primary = Color(0xFFE91E63), // Example Pink
//    secondary = Color(0xFFF06292),
//    tertiary = Color(0xFFF48FB1),
//    background = Color(0xFFFFFBFA), // Example light pinkish background
//    surface = Color(0xFFFFFBFA),
//    onPrimary = Color.White,
//    onSecondary = Color.Black,
//    onTertiary = Color.Black,
//    onBackground = Color(0xFF1C1B1F),
//    onSurface = Color(0xFF1C1B1F),
//    surfaceVariant = Color(0xFFFCE4EC), // Lighter pink variant
//    onSurfaceVariant = Color(0xFF504346),
//    outline = Color(0xFF817377),
//    primaryContainer = Color(0xFFFFD9E2), // Pink container
//    onPrimaryContainer = Color(0xFF3E001D)
// ... define other colors as needed
)


// --- Shapes --- (Keep existing)
val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

// --- Typography --- (Keep existing)
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

// --- Theme Option Enum --- (Keep existing - THIS IS THE CORRECT ONE)
enum class ThemeOption {
    Light, Dark, Pink, System
}


// --- SmartHomeTheme Composable ---
@Composable
fun SmartHomeTheme(
    // CORRECTED: Use the local ThemeOption type without the wrong package qualifier
    selectedTheme: ThemeOption = ThemeOption.Light,
    dynamicColor: Boolean = false, // Dynamic color support (keep or remove based on need)
    content: @Composable (() -> Unit)
) {
    // This logic now correctly uses the local 'com.example.smarthome.ui.theme.ThemeOption'
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            // Use dynamic color scheme based on system theme if requested and available
            if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Select appropriate *RENAMED* custom theme based on the CORRECT selectedTheme type
        selectedTheme == ThemeOption.Dark -> MyDarkColorScheme
        selectedTheme == ThemeOption.Pink -> MyPinkColorScheme
        else -> MyLightColorScheme // Default to Light
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window // Safer cast to Activity
            if (window != null) { // Check if window is not null
                window.statusBarColor = colorScheme.background.toArgb() // Use background color for status bar
                // Set status bar icon colors based on the selected theme (light icons for dark themes)
                // The comparisons now work correctly
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    selectedTheme == ThemeOption.Light || selectedTheme == ThemeOption.Pink
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes, // Apply custom shapes
        content = content // The actual UI content
    )
}
