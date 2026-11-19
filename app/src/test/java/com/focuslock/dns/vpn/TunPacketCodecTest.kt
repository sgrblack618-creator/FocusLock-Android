package com.focuslock.dns.vpn

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TunPacketCodecTest {

    @Test
    fun parsesAndBuildsValidIpv4DnsResponse() {
        val dnsQuery = byteArrayOf(
            0x12, 0x34, // Transaction ID
            0x01, 0x00, // Standard query, RD=1
            0x00, 0x01, // QDCOUNT=1
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x08, 'x'.code.toByte(), 'h'.code.toByte(), 'a'.code.toByte(),
            'm'.code.toByte(), 's'.code.toByte(), 't'.code.toByte(), 'e'.code.toByte(), 'r'.code.toByte(),
            0x03, 'c'.code.toByte(), 'o'.code.toByte(), 'm'.code.toByte(),
            0x00, 0x00, 0x01, 0x00, 0x01
        )
        val udpLen = 8 + dnsQuery.size
        val totalLen = 20 + udpLen
        val packet = ByteArray(totalLen)
        packet[0] = 0x45
        packet[2] = ((totalLen ushr 8) and 0xFF).toByte()
        packet[3] = (totalLen and 0xFF).toByte()
        packet[8] = 64
        packet[9] = 17 // UDP
        // Src IP 10.89.0.1 -> Dst IP 10.89.0.2
        packet[12] = 10; packet[13] = 89; packet[14] = 0; packet[15] = 1
        packet[16] = 10; packet[17] = 89; packet[18] = 0; packet[19] = 2
        // Src Port 51000, Dst Port 53
        packet[20] = (51000 ushr 8).toByte(); packet[21] = (51000 and 0xFF).toByte()
        packet[22] = 0; packet[23] = 53
        packet[24] = ((udpLen ushr 8) and 0xFF).toByte(); packet[25] = (udpLen and 0xFF).toByte()
        System.arraycopy(dnsQuery, 0, packet, 28, dnsQuery.size)

        val parsed = TunPacketCodec.parseDnsPacket(packet, totalLen)
        assertNotNull(parsed)
        assertEquals(4, parsed!!.ipVersion)
        assertEquals(51000, parsed.srcPort)
        assertEquals(53, parsed.dstPort)
        assertArrayEquals(dnsQuery, parsed.dnsPayload)

        val servFail = TunPacketCodec.buildServFailDnsResponse(parsed.dnsPayload)
        assertEquals(0x12.toByte(), servFail[0])
        assertEquals(0x34.toByte(), servFail[1])
        assertEquals(0x82.toByte(), servFail[3]) // RA=1, RCODE=2 (SERVFAIL)

        val responsePacket = TunPacketCodec.buildResponsePacket(parsed, servFail)
        assertEquals(4, (responsePacket[0].toInt() ushr 4) and 0x0F)
        assertEquals(10.toByte(), responsePacket[12]) // Swapped Src IP = 10.89.0.2
        assertEquals(2.toByte(), responsePacket[15])
        assertEquals(53, ((responsePacket[20].toInt() and 0xFF) shl 8) or (responsePacket[21].toInt() and 0xFF))
    }

    @Test
    fun parsesAndBuildsValidIpv6DnsResponse() {
        val dnsQuery = ByteArray(16).apply {
            this[0] = 0x55
            this[1] = 0x66
            this[2] = 0x01
            this[5] = 0x01
        }
        val udpLen = 8 + dnsQuery.size
        val totalLen = 40 + udpLen
        val packet = ByteArray(totalLen)
        packet[0] = 0x60
        packet[4] = ((udpLen ushr 8) and 0xFF).toByte()
        packet[5] = (udpLen and 0xFF).toByte()
        packet[6] = 17 // UDP
        packet[7] = 64
        packet[8] = 0xfd.toByte(); packet[9] = 0x89.toByte(); packet[23] = 1
        packet[24] = 0xfd.toByte(); packet[25] = 0x89.toByte(); packet[39] = 2
        packet[40] = 0x30; packet[41] = 0x39 // src port 12345
        packet[42] = 0x00; packet[43] = 53 // dst port 53
        packet[44] = ((udpLen ushr 8) and 0xFF).toByte(); packet[45] = (udpLen and 0xFF).toByte()
        System.arraycopy(dnsQuery, 0, packet, 48, dnsQuery.size)

        val parsed = TunPacketCodec.parseDnsPacket(packet, totalLen)
        assertNotNull(parsed)
        assertEquals(6, parsed!!.ipVersion)
        assertEquals(12345, parsed.srcPort)

        val responsePacket = TunPacketCodec.buildResponsePacket(parsed, dnsQuery)
        assertEquals(6, (responsePacket[0].toInt() ushr 4) and 0x0F)
        assertEquals(2.toByte(), responsePacket[23]) // Swapped Src IP ends in ::2
        assertEquals(1.toByte(), responsePacket[39]) // Swapped Dst IP ends in ::1
    }

    @Test
    fun rejectsNonDnsTraffic() {
        val nonDns = ByteArray(40).apply {
            this[0] = 0x45
            this[9] = 6 // TCP
            this[22] = 0x01
            this[23] = 0xBB.toByte() // Port 443
        }
        assertNull(TunPacketCodec.parseDnsPacket(nonDns, 40))
    }
}
