package com.focuslock.dns.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LockTimerAndProfileTest {

    @Test
    fun timerComputesDaysAndHoursRemainingCorrectly() {
        val twoDaysFiveHoursMs =
            (2 * LockTimerState.MILLIS_PER_DAY) + (5 * LockTimerState.MILLIS_PER_HOUR)
        val state = LockTimerState(
            isFocusLockActive = true,
            startedAtEpochMs = 1_000L,
            expiresAtEpochMs = 1_000L + twoDaysFiveHoursMs,
            durationMs = twoDaysFiveHoursMs,
            remainingMs = twoDaysFiveHoursMs
        )

        assertTrue(state.isFocusLockActive)
        assertEquals(2L, state.remainingDays)
        assertEquals(5L, state.remainingHoursPart)
        assertEquals(1.0f, state.progressFraction, 0.001f)
    }

    @Test
    fun profilesMatchExactAdGuardDoHEndpoints() {
        assertEquals("https://dns.adguard-dns.com/dns-query", DnsProfile.NORMAL.dohUrl)
        assertEquals("https://family.adguard-dns.com/dns-query", DnsProfile.FOCUS_LOCK.dohUrl)
        assertFalse(DnsProfile.NORMAL.bootstrapIps.isEmpty())
        assertFalse(DnsProfile.FOCUS_LOCK.bootstrapIps.isEmpty())
    }
}
