package com.focuslock.dns.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focuslock.dns.data.FocusLockPreferences
import com.focuslock.dns.data.MotivationalReminders
import com.focuslock.dns.ui.strings.BilingualStrings
import com.focuslock.dns.ui.theme.FocusLockColors
import com.focuslock.dns.ui.theme.FocusLockTheme
import kotlinx.coroutines.delay

/**
 * Mindful Islamic & Motivational reflection screen displayed when Focus Lock blocks an adult domain
 * or when the user opens the Emergency Reflection Shield.
 */
class BlockReminderActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = FocusLockPreferences(this)
        val strings = BilingualStrings.forCode(prefs.appLanguage)
        val quoteIndex = intent.getIntExtra(EXTRA_QUOTE_INDEX, 0)
        val quote = MotivationalReminders.quoteForIndex(quoteIndex)
        val isBn = prefs.appLanguage == FocusLockPreferences.LANG_BN
        val commitmentNote = prefs.personalCommitmentNote

        setContent {
            FocusLockTheme {
                var waitSeconds by remember { mutableIntStateOf(5) }
                val scrollState = rememberScrollState()

                LaunchedEffect(Unit) {
                    while (waitSeconds > 0) {
                        delay(1_000L)
                        waitSeconds--
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(FocusLockColors.Canvas)
                        .verticalScroll(scrollState)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(FocusLockColors.FocusEmeraldSoft, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = FocusLockColors.FocusEmerald,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = strings.reminderScreenTitle,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = FocusLockColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.reminderScreenSubtitle,
                        fontSize = 13.sp,
                        color = FocusLockColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Quote Visual Card with bundled artwork backdrop
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = FocusLockColors.Surface,
                        border = BorderStroke(1.dp, FocusLockColors.FocusEmerald.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            quote.imageResId?.let { resId ->
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(RoundedCornerShape(20.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0xD9090D12),
                                                    Color(0xE8090D12)
                                                )
                                            )
                                        )
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isBn) quote.titleBn else quote.titleEn,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FocusLockColors.FocusEmerald,
                                    textAlign = TextAlign.Center
                                )
                                if (quote.arabicLine != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = quote.arabicLine,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FocusLockColors.TextPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (isBn) quote.bodyBn else quote.bodyEn,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    color = FocusLockColors.TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (isBn) quote.sourceBn else quote.sourceEn,
                                    fontSize = 12.sp,
                                    color = FocusLockColors.TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    if (commitmentNote.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = FocusLockColors.Surface,
                            border = BorderStroke(1.dp, FocusLockColors.Hairline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = strings.commitmentHeader,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FocusLockColors.NormalSky
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = commitmentNote,
                                    fontSize = 13.sp,
                                    color = FocusLockColors.TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Practical Reset Actions
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FocusLockColors.Surface,
                        border = BorderStroke(1.dp, FocusLockColors.Hairline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = strings.reminderActionStepsTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FocusLockColors.TextPrimary
                            )
                            Text(text = strings.reminderActionStep1, fontSize = 12.sp, color = FocusLockColors.TextSecondary)
                            Text(text = strings.reminderActionStep2, fontSize = 12.sp, color = FocusLockColors.TextSecondary)
                            Text(text = strings.reminderActionStep3, fontSize = 12.sp, color = FocusLockColors.TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(homeIntent)
                            finish()
                        },
                        enabled = waitSeconds == 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FocusLockColors.FocusEmerald,
                            contentColor = FocusLockColors.Canvas,
                            disabledContainerColor = FocusLockColors.SurfaceElevated,
                            disabledContentColor = FocusLockColors.TextMuted
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = if (waitSeconds > 0) {
                                "${strings.reminderWaitPrefix} (${waitSeconds}s)"
                            } else {
                                strings.reminderReturnBtn
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = strings.reminderStayAwayNote,
                        fontSize = 12.sp,
                        color = FocusLockColors.TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_QUOTE_INDEX = "extra_quote_index"
    }
}
