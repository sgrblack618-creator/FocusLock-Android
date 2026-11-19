package com.focuslock.dns.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object FocusLockColors {
    val Canvas = Color(0xFF090D12)
    val Surface = Color(0xFF111821)
    val SurfaceElevated = Color(0xFF17212D)
    val Hairline = Color(0xFF1F2A37)
    val TextPrimary = Color(0xFFF3F6FA)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)
    val FocusEmerald = Color(0xFF10B981)
    val FocusEmeraldSoft = Color(0x1F10B981)
    val NormalSky = Color(0xFF38BDF8)
    val NormalSkySoft = Color(0x1F38BDF8)
    val WarningAmber = Color(0xFFF59E0B)
    val WarningAmberSoft = Color(0x1FF59E0B)
    val DangerRed = Color(0xFFEF4444)
    val DangerRedSoft = Color(0x1FEF4444)
}

private val FocusLockDarkScheme = darkColorScheme(
    primary = FocusLockColors.FocusEmerald,
    onPrimary = Color(0xFF04140E),
    secondary = FocusLockColors.NormalSky,
    onSecondary = Color(0xFF061621),
    background = FocusLockColors.Canvas,
    onBackground = FocusLockColors.TextPrimary,
    surface = FocusLockColors.Surface,
    onSurface = FocusLockColors.TextPrimary,
    surfaceVariant = FocusLockColors.SurfaceElevated,
    onSurfaceVariant = FocusLockColors.TextSecondary,
    outline = FocusLockColors.Hairline,
    error = FocusLockColors.DangerRed,
    onError = Color.White
)

private val FocusLockTypography = Typography(
    headlineMedium = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 32.sp,
        color = FocusLockColors.TextPrimary
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp,
        color = FocusLockColors.TextPrimary
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 21.sp,
        color = FocusLockColors.TextSecondary
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
        color = FocusLockColors.TextSecondary
    )
)

@Composable
fun FocusLockTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FocusLockDarkScheme,
        typography = FocusLockTypography,
        content = content
    )
}
