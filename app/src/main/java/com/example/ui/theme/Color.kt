package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Warm Earthy Canvas & Neutrals (NO generic tech blues)
val WarmCreamBg = Color(0xFFFFF8EC)
val SoftOatSurface = Color(0xFFF6EBD5)
val ElevatedClayCard = Color(0xFFFFFDF7)
val WarmSandBorder = Color(0xFFE7D3B3)
val EspressoInk = Color(0xFF3A2618)
val CocoaSecondary = Color(0xFF6B4E37)
val ClayShadowTint = Color(0xFF8C6239)

// Status & Semantic Earthy Accents
val LeafGreen = Color(0xFF4F8A4B)
val LeafGreenSoft = Color(0xFFDDF0D8)
val SunAmber = Color(0xFFE89B25)
val SunAmberSoft = Color(0xFFFFF0C7)
val TerracottaCoral = Color(0xFFD45D3A)
val TerracottaSoft = Color(0xFFFFE2D6)
val ClayError = Color(0xFFC44536)
val ClayErrorContainer = Color(0xFFFFDAD4)

// Metallic Educator Certificate Colors
val BronzeLight = Color(0xFFE6A972)
val BronzeMid = Color(0xFFB87333)
val BronzeDark = Color(0xFF7D491B)

val SilverLight = Color(0xFFF2F0EA)
val SilverMid = Color(0xFFBDB9B0)
val SilverDark = Color(0xFF7D7970)

val GoldLight = Color(0xFFFFE885)
val GoldMid = Color(0xFFE3A82B)
val GoldDark = Color(0xFF9C6B10)

enum class StudentThemeColor(
    val key: String,
    val label: String,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val tertiary: Color
) {
    TERRACOTTA(
        key = "terracotta",
        label = "Terracotta Sun",
        primary = Color(0xFFD4622A),
        onPrimary = Color(0xFFFFFBF5),
        primaryContainer = Color(0xFFFFE0CE),
        onPrimaryContainer = Color(0xFF4A1D06),
        secondary = Color(0xFF5C8A4D),
        secondaryContainer = Color(0xFFDCEFD3),
        tertiary = Color(0xFFE6A127)
    ),
    MARIGOLD(
        key = "marigold",
        label = "Marigold Glow",
        primary = Color(0xFFD68810),
        onPrimary = Color(0xFFFFFBF2),
        primaryContainer = Color(0xFFFFEAC2),
        onPrimaryContainer = Color(0xFF472B00),
        secondary = Color(0xFF6B8E4E),
        secondaryContainer = Color(0xFFE1EED5),
        tertiary = Color(0xFFC85D32)
    ),
    SAGE(
        key = "sage",
        label = "Forest Sage",
        primary = Color(0xFF49854E),
        onPrimary = Color(0xFFF9FFF8),
        primaryContainer = Color(0xFFD7F0D6),
        onPrimaryContainer = Color(0xFF0E3814),
        secondary = Color(0xFFD6882B),
        secondaryContainer = Color(0xFFFFE9C9),
        tertiary = Color(0xFFB65A38)
    ),
    CINNAMON(
        key = "cinnamon",
        label = "Cinnamon Clay",
        primary = Color(0xFFB54E35),
        onPrimary = Color(0xFFFFF9F7),
        primaryContainer = Color(0xFFFFDDD3),
        onPrimaryContainer = Color(0xFF451408),
        secondary = Color(0xFF6A8D4C),
        secondaryContainer = Color(0xFFE0EED4),
        tertiary = Color(0xFFE0A430)
    ),
    PLUM(
        key = "plum",
        label = "Warm Berry",
        primary = Color(0xFF9C4768),
        onPrimary = Color(0xFFFFF8FA),
        primaryContainer = Color(0xFFFFD9E5),
        onPrimaryContainer = Color(0xFF401023),
        secondary = Color(0xFF5D8A4E),
        secondaryContainer = Color(0xFFDCEFD4),
        tertiary = Color(0xFFE39E2E)
    );

    companion object {
        fun fromKey(key: String): StudentThemeColor =
            entries.firstOrNull { it.key == key } ?: TERRACOTTA
    }
}
