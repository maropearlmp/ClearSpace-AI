package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryBlue,
    secondary = SafeGreen,
    tertiary = WarningRed,
    background = AppBackground,
    surface = AppSurface,
    onPrimary = OnPrimaryBlue,
    onBackground = AppOnSurface,
    onSurface = AppOnSurface,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = AppOnSurfaceVariant,
    primaryContainer = PrimaryBlueContainer,
    secondaryContainer = SafeGreenContainer,
    onSecondaryContainer = OnSafeGreenContainer,
    tertiaryContainer = WarningRedContainer,
    onTertiaryContainer = OnWarningRedContainer
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryBlue,
    secondary = SafeGreen,
    tertiary = WarningRed,
    background = AppBackground,
    surface = AppSurface,
    onPrimary = OnPrimaryBlue,
    onBackground = AppOnSurface,
    onSurface = AppOnSurface,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = AppOnSurfaceVariant,
    outline = AppOutline,
    outlineVariant = AppOutlineVariant,
    primaryContainer = PrimaryBlueContainer,
    secondaryContainer = SafeGreenContainer,
    onSecondaryContainer = OnSafeGreenContainer,
    tertiaryContainer = WarningRedContainer,
    onTertiaryContainer = OnWarningRedContainer
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color to enforce our precise brand guidelines from Stitch
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
