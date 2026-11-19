package com.focuslock.dns.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focuslock.dns.model.LockTimerState
import com.focuslock.dns.model.VpnConnectionState
import com.focuslock.dns.ui.strings.AppStrings
import com.focuslock.dns.ui.theme.FocusLockColors
import java.util.Locale

@Composable
fun CircularCountdownRing(
    vpnState: VpnConnectionState,
    timerState: LockTimerState,
    selectedDurationLabel: String,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val isVpnActive = vpnState == VpnConnectionState.ACTIVE
    val ringColor = when {
        !isVpnActive -> FocusLockColors.DangerRed
        timerState.isFocusLockActive -> FocusLockColors.FocusEmerald
        else -> FocusLockColors.NormalSky
    }
    val sweepAngle = when {
        !isVpnActive -> 0f
        timerState.isFocusLockActive -> 360f * timerState.progressFraction
        else -> 360f
    }

    Box(
        modifier = modifier.size(236.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(236.dp)) {
            val strokeWidth = 14.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)

            drawArc(
                color = FocusLockColors.Hairline,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            if (sweepAngle > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when {
                    !isVpnActive -> strings.ringStopped
                    timerState.isFocusLockActive -> strings.ringLocked
                    else -> strings.ringNormal
                },
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ringColor
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (timerState.isFocusLockActive) {
                val days = timerState.remainingDays
                val hours = timerState.remainingHoursPart
                val mins = timerState.remainingMinutesPart
                val secs = timerState.remainingSecondsPart

                Text(
                    text = String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs),
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = FocusLockColors.TextPrimary,
                        fontFeatureSettings = "tnum"
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${days}${strings.daysUnit} ${hours}${strings.hoursUnit} ${strings.remainingSuffix}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = FocusLockColors.TextSecondary,
                        fontFeatureSettings = "tnum"
                    )
                )
            } else {
                Text(
                    text = if (isVpnActive) strings.ringAdBlock else "0${strings.daysUnit} 0${strings.hoursUnit}",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = FocusLockColors.TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isVpnActive) {
                        "${strings.selectedLockPrefix} $selectedDurationLabel"
                    } else {
                        strings.ringTapToStart
                    },
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = FocusLockColors.TextSecondary
                    )
                )
            }
        }
    }
}
