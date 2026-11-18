package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.model.ThemePreference

private val PranayamaDarkColorScheme = darkColorScheme(
    primary = MintDarkPrimary,
    onPrimary = OnMintDarkPrimary,
    primaryContainer = DeepTealDarkContainer,
    onPrimaryContainer = OnDeepTealDarkContainer,
    secondary = SoftTealDarkSecondary,
    onSecondary = OnSoftTealDarkSecondary,
    secondaryContainer = DarkTealSecondaryContainer,
    onSecondaryContainer = OnDarkTealSecondaryContainer,
    tertiary = WarmSandDarkTertiary,
    onTertiary = OnWarmSandDarkTertiary,
    tertiaryContainer = DarkTerracottaContainer,
    onTertiaryContainer = OnDarkTerracottaContainer,
    background = ForestNightBackground,
    onBackground = MistOnDarkBackground,
    surface = DeepMossSurface,
    onSurface = MistOnDarkBackground,
    surfaceVariant = DarkMossSurfaceVariant,
    onSurfaceVariant = SageGrayOnDarkSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

private val PranayamaLightColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = OnSagePrimary,
    primaryContainer = SagePrimaryContainer,
    onPrimaryContainer = OnSagePrimaryContainer,
    secondary = TealSecondary,
    onSecondary = OnTealSecondary,
    secondaryContainer = TealSecondaryContainer,
    onSecondaryContainer = OnTealSecondaryContainer,
    tertiary = TerracottaTertiary,
    onTertiary = OnTerracottaTertiary,
    tertiaryContainer = TerracottaTertiaryContainer,
    onTertiaryContainer = OnTerracottaTertiaryContainer,
    background = CreamBackground,
    onBackground = InkOnBackground,
    surface = CreamSurface,
    onSurface = InkOnBackground,
    surfaceVariant = SoftSurfaceVariant,
    onSurfaceVariant = MutedOnSurfaceVariant,
    outline = SubtleOutline,
    outlineVariant = SubtleOutlineVariant
)

val PranayamaShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun PranayamaTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    val colorScheme = if (darkTheme) PranayamaDarkColorScheme else PranayamaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PranayamaTypography,
        shapes = PranayamaShapes,
        content = content
    )
}
