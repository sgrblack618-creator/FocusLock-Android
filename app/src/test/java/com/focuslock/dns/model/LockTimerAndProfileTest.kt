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
    fun profilesMatchExactEncryptedDoHEndpoints() {
        assertEquals("https://dns.adguard-dns.com/dns-query", DnsProfile.NORMAL.dohUrl)
        assertEquals("https://security.cloudflare-dns.com/dns-query", DnsProfile.NORMAL_NO_ADBLOCK.dohUrl)
        assertEquals("https://family.adguard-dns.com/dns-query", DnsProfile.FOCUS_LOCK.dohUrl)
        assertEquals("https://family.cloudflare-dns.com/dns-query", DnsProfile.FOCUS_LOCK_NO_ADBLOCK.dohUrl)

        assertFalse(DnsProfile.NORMAL.isFocusLockAdultFilter)
        assertTrue(DnsProfile.NORMAL.isAdBlockEnabled)
        assertFalse(DnsProfile.NORMAL_NO_ADBLOCK.isFocusLockAdultFilter)
        assertFalse(DnsProfile.NORMAL_NO_ADBLOCK.isAdBlockEnabled)

        assertTrue(DnsProfile.FOCUS_LOCK.isFocusLockAdultFilter)
        assertTrue(DnsProfile.FOCUS_LOCK.isAdBlockEnabled)
        assertTrue(DnsProfile.FOCUS_LOCK_NO_ADBLOCK.isFocusLockAdultFilter)
        assertFalse(DnsProfile.FOCUS_LOCK_NO_ADBLOCK.isAdBlockEnabled)

        DnsProfile.entries.forEach { profile ->
            assertFalse(profile.bootstrapIps.isEmpty())
        }
    }
}
