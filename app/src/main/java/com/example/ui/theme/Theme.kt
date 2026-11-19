package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val TwogetherShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun TwogetherTheme(
    themeColorKey: String = StudentThemeColor.TERRACOTTA.key,
    content: @Composable () -> Unit
) {
    val palette = StudentThemeColor.fromKey(themeColorKey)
    val colorScheme = lightColorScheme(
        primary = palette.primary,
        onPrimary = palette.onPrimary,
        primaryContainer = palette.primaryContainer,
        onPrimaryContainer = palette.onPrimaryContainer,
        secondary = palette.secondary,
        onSecondary = palette.onPrimary,
        secondaryContainer = palette.secondaryContainer,
        onSecondaryContainer = EspressoInk,
        tertiary = palette.tertiary,
        onTertiary = EspressoInk,
        tertiaryContainer = SunAmberSoft,
        onTertiaryContainer = EspressoInk,
        background = WarmCreamBg,
        onBackground = EspressoInk,
        surface = ElevatedClayCard,
        onSurface = EspressoInk,
        surfaceVariant = SoftOatSurface,
        onSurfaceVariant = CocoaSecondary,
        outline = WarmSandBorder,
        error = ClayError,
        onError = palette.onPrimary,
        errorContainer = ClayErrorContainer,
        onErrorContainer = EspressoInk
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TwogetherTypography,
        shapes = TwogetherShapes,
        content = content
    )
}
