package com.focuslock.dns.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MotivationalReminderTest {

    @Test
    fun detectsNxDomainAndZeroIpBlockedResponses() {
        // NXDOMAIN (RCODE = 3)
        val nxDomain = ByteArray(12).apply {
            this[3] = 0x83.toByte()
        }
        assertTrue(MotivationalReminders.isBlockedDnsResponse(nxDomain))

        // Allowed A record pointing to 93.184.216.34
        val allowedResponse = byteArrayOf(
            0x12, 0x34, 0x81.toByte(), 0x80.toByte(),
            0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,
            0x07, 'e'.code.toByte(), 'x'.code.toByte(), 'a'.code.toByte(),
            'm'.code.toByte(), 'p'.code.toByte(), 'l'.code.toByte(), 'e'.code.toByte(),
            0x03, 'c'.code.toByte(), 'o'.code.toByte(), 'm'.code.toByte(),
            0x00, 0x00, 0x01, 0x00, 0x01,
            // Answer with pointer 0xC0 0x0C, Type A (1), Class IN (1), TTL, Len 4, IP 93.184.216.34
            0xC0.toByte(), 0x0C, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x3C,
            0x00, 0x04, 93.toByte(), 184.toByte(), 216.toByte(), 34.toByte()
        )
        assertFalse(MotivationalReminders.isBlockedDnsResponse(allowedResponse))

        // Blocked A record pointing to 0.0.0.0
        val blockedZeroIp = allowedResponse.copyOf().apply {
            this[size - 4] = 0
            this[size - 3] = 0
            this[size - 2] = 0
            this[size - 1] = 0
        }
        assertTrue(MotivationalReminders.isBlockedDnsResponse(blockedZeroIp))
    }

    @Test
    fun detectsAdultDomainQueryOnlyInRam() {
        val xhamsterQuery = byteArrayOf(
            0x12, 0x34, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x08, 'x'.code.toByte(), 'h'.code.toByte(), 'a'.code.toByte(),
            'm'.code.toByte(), 's'.code.toByte(), 't'.code.toByte(), 'e'.code.toByte(), 'r'.code.toByte(),
            0x03, 'c'.code.toByte(), 'o'.code.toByte(), 'm'.code.toByte(),
            0x00, 0x00, 0x01, 0x00, 0x01
        )
        assertTrue(MotivationalReminders.isAdultCategoryQueryInRam(xhamsterQuery))

        val normalAdQuery = byteArrayOf(
            0x12, 0x34, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x06, 'g'.code.toByte(), 'o'.code.toByte(), 'o'.code.toByte(),
            'g'.code.toByte(), 'l'.code.toByte(), 'e'.code.toByte(),
            0x03, 'c'.code.toByte(), 'o'.code.toByte(), 'm'.code.toByte(),
            0x00, 0x00, 0x01, 0x00, 0x01
        )
        assertFalse(MotivationalReminders.isAdultCategoryQueryInRam(normalAdQuery))
    }
}
