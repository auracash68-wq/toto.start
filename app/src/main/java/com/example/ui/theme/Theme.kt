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

private val CyberpunkColorScheme = darkColorScheme(
  primary = NeonGreen,
  secondary = GraphiteGray,
  background = DeepCharcoal,
  surface = GraphiteGray,
  error = CrimsonRed,
  onPrimary = DeepCharcoal,
  onSecondary = TextWhite,
  onBackground = TextWhite,
  onSurface = TextWhite,
  outline = BorderGray
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = CyberpunkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
