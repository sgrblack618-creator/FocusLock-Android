package com.focuslock.dns.vpn

/**
 * Pure Kotlin, low-allocation IPv4 & IPv6 UDP/DNS TUN packet parser and serializer.
 * Computes exact RFC 791 (IPv4) and RFC 768 / RFC 8200 (IPv4 & IPv6 UDP) checksums.
 */
object TunPacketCodec {

    private const val PROTOCOL_UDP = 17
    private const val DNS_PORT = 53

    data class ParsedDnsPacket(
        val ipVersion: Int,
        val srcIp: ByteArray,
        val dstIp: ByteArray,
        val srcPort: Int,
        val dstPort: Int,
        val dnsPayload: ByteArray
    )

    fun parseDnsPacket(packet: ByteArray, length: Int): ParsedDnsPacket? {
        if (length < 28) return null
        val version = (packet[0].toInt() ushr 4) and 0x0F
        return when (version) {
            4 -> parseIpv4UdpDns(packet, length)
            6 -> parseIpv6UdpDns(packet, length)
            else -> null
        }
    }

    private fun parseIpv4UdpDns(packet: ByteArray, length: Int): ParsedDnsPacket? {
        val ihlBytes = (packet[0].toInt() and 0x0F) * 4
        if (ihlBytes < 20 || length < ihlBytes + 8) return null

        val protocol = packet[9].toInt() and 0xFF
        if (protocol != PROTOCOL_UDP) return null

        val srcIp = packet.copyOfRange(12, 16)
        val dstIp = packet.copyOfRange(16, 20)

        val srcPort = readUInt16(packet, ihlBytes)
        val dstPort = readUInt16(packet, ihlBytes + 2)
        if (dstPort != DNS_PORT) return null

        val udpLength = readUInt16(packet, ihlBytes + 4)
        if (udpLength < 8 || ihlBytes + udpLength > length) return null

        val dnsStart = ihlBytes + 8
        val dnsEnd = ihlBytes + udpLength
        if (dnsEnd - dnsStart < 12) return null

        return ParsedDnsPacket(
            ipVersion = 4,
            srcIp = srcIp,
            dstIp = dstIp,
            srcPort = srcPort,
            dstPort = dstPort,
            dnsPayload = packet.copyOfRange(dnsStart, dnsEnd)
        )
    }

    private fun parseIpv6UdpDns(packet: ByteArray, length: Int): ParsedDnsPacket? {
        if (length < 48) return null
        val srcIp = packet.copyOfRange(8, 24)
        val dstIp = packet.copyOfRange(24, 40)

        var nextHeader = packet[6].toInt() and 0xFF
        var offset = 40

        // Walk standard IPv6 extension headers if present
        while (nextHeader == 0 || nextHeader == 43 || nextHeader == 60) {
            if (offset + 2 > length) return null
            val headerLen = ((packet[offset + 1].toInt() and 0xFF) + 1) * 8
            if (headerLen <= 0 || offset + headerLen > length) return null
            nextHeader = packet[offset].toInt() and 0xFF
            offset += headerLen
        }

        if (nextHeader != PROTOCOL_UDP || length < offset + 8) return null

        val srcPort = readUInt16(packet, offset)
        val dstPort = readUInt16(packet, offset + 2)
        if (dstPort != DNS_PORT) return null

        val udpLength = readUInt16(packet, offset + 4)
        if (udpLength < 8 || offset + udpLength > length) return null

        val dnsStart = offset + 8
        val dnsEnd = offset + udpLength
        if (dnsEnd - dnsStart < 12) return null

        return ParsedDnsPacket(
            ipVersion = 6,
            srcIp = srcIp,
            dstIp = dstIp,
            srcPort = srcPort,
            dstPort = dstPort,
            dnsPayload = packet.copyOfRange(dnsStart, dnsEnd)
        )
    }

    fun buildResponsePacket(request: ParsedDnsPacket, dnsResponse: ByteArray): ByteArray {
        return when (request.ipVersion) {
            4 -> buildIpv4Response(request, dnsResponse)
            6 -> buildIpv6Response(request, dnsResponse)
            else -> throw IllegalArgumentException("Unsupported IP version: ${request.ipVersion}")
        }
    }

    private fun buildIpv4Response(request: ParsedDnsPacket, dnsResponse: ByteArray): ByteArray {
        val ipHeaderLen = 20
        val udpLen = 8 + dnsResponse.size
        val totalLen = ipHeaderLen + udpLen
        val out = ByteArray(totalLen)

        // IPv4 Header
        out[0] = 0x45.toByte() // Version 4, IHL 5 (20 bytes)
        out[1] = 0x00
        writeUInt16(out, 2, totalLen)
        out[4] = 0x00
        out[5] = 0x00
        out[6] = 0x40.toByte() // Don't Fragment
        out[7] = 0x00
        out[8] = 64.toByte() // TTL
        out[9] = PROTOCOL_UDP.toByte()
        // Checksum at 10..11 filled below
        System.arraycopy(request.dstIp, 0, out, 12, 4) // Swap dst -> src
        System.arraycopy(request.srcIp, 0, out, 16, 4) // Swap src -> dst

        val ipChecksum = computeIpv4HeaderChecksum(out, ipHeaderLen)
        writeUInt16(out, 10, ipChecksum)

        // UDP Header
        writeUInt16(out, ipHeaderLen, request.dstPort) // 53
        writeUInt16(out, ipHeaderLen + 2, request.srcPort)
        writeUInt16(out, ipHeaderLen + 4, udpLen)
        // UDP checksum at ipHeaderLen + 6..7 filled below
        System.arraycopy(dnsResponse, 0, out, ipHeaderLen + 8, dnsResponse.size)

        val udpChecksum = computeUdpChecksum(
            isIpv6 = false,
            srcIp = out.copyOfRange(12, 16),
            dstIp = out.copyOfRange(16, 20),
            udpSegment = out,
            udpOffset = ipHeaderLen,
            udpLength = udpLen
        )
        writeUInt16(out, ipHeaderLen + 6, udpChecksum)

        return out
    }

    private fun buildIpv6Response(request: ParsedDnsPacket, dnsResponse: ByteArray): ByteArray {
        val ipHeaderLen = 40
        val udpLen = 8 + dnsResponse.size
        val out = ByteArray(ipHeaderLen + udpLen)

        // IPv6 Header
        out[0] = 0x60.toByte() // Version 6
        out[1] = 0x00
        out[2] = 0x00
        out[3] = 0x00
        writeUInt16(out, 4, udpLen) // Payload length
        out[6] = PROTOCOL_UDP.toByte() // Next header
        out[7] = 64.toByte() // Hop limit
        System.arraycopy(request.dstIp, 0, out, 8, 16)  // Swap dst -> src
        System.arraycopy(request.srcIp, 0, out, 24, 16) // Swap src -> dst

        // UDP Header
        writeUInt16(out, ipHeaderLen, request.dstPort)
        writeUInt16(out, ipHeaderLen + 2, request.srcPort)
        writeUInt16(out, ipHeaderLen + 4, udpLen)
        System.arraycopy(dnsResponse, 0, out, ipHeaderLen + 8, dnsResponse.size)

        val udpChecksum = computeUdpChecksum(
            isIpv6 = true,
            srcIp = out.copyOfRange(8, 24),
            dstIp = out.copyOfRange(24, 40),
            udpSegment = out,
            udpOffset = ipHeaderLen,
            udpLength = udpLen
        )
        writeUInt16(out, ipHeaderLen + 6, udpChecksum)

        return out
    }

    /**
     * Generates a valid RFC 1035 SERVFAIL (RCODE = 2) response so failed encrypted DoH requests
     * fail closed immediately without falling back to any unrestricted DNS resolver.
     */
    fun buildServFailDnsResponse(dnsQuery: ByteArray): ByteArray {
        if (dnsQuery.size < 12) return ByteArray(0)
        val out = dnsQuery.copyOf()
        // Byte 2: set QR = 1 (response), preserve OPCODE & RD, clear TC & AA
        val reqFlagsHi = dnsQuery[2].toInt() and 0xFF
        out[2] = ((reqFlagsHi and 0x79) or 0x80).toByte()
        // Byte 3: set RA = 1, RCODE = 2 (SERVFAIL)
        out[3] = 0x82.toByte()
        // Zero ANCOUNT, NSCOUNT, ARCOUNT (bytes 6..11)
        for (i in 6 until 12) {
            out[i] = 0
        }
        return out
    }

    /**
     * Generates a valid RFC 1035 NXDOMAIN (RCODE = 3) response in RAM for user-selected
     * local block rules (e.g. gambling/betting or social distraction toggles).
     */
    fun buildNxDomainDnsResponse(dnsQuery: ByteArray): ByteArray {
        if (dnsQuery.size < 12) return ByteArray(0)
        val out = dnsQuery.copyOf()
        val reqFlagsHi = dnsQuery[2].toInt() and 0xFF
        out[2] = ((reqFlagsHi and 0x79) or 0x80).toByte()
        // Byte 3: set RA = 1, RCODE = 3 (NXDOMAIN)
        out[3] = 0x83.toByte()
        for (i in 6 until 12) {
            out[i] = 0
        }
        return out
    }

    private fun computeIpv4HeaderChecksum(header: ByteArray, length: Int): Int {
        var sum = 0L
        var i = 0
        while (i < length) {
            if (i != 10) {
                sum += ((header[i].toInt() and 0xFF) shl 8) or (header[i + 1].toInt() and 0xFF)
            }
            i += 2
        }
        while ((sum ushr 16) != 0L) {
            sum = (sum and 0xFFFF) + (sum ushr 16)
        }
        return (sum.inv() and 0xFFFF).toInt()
    }

    private fun computeUdpChecksum(
        isIpv6: Boolean,
        srcIp: ByteArray,
        dstIp: ByteArray,
        udpSegment: ByteArray,
        udpOffset: Int,
        udpLength: Int
    ): Int {
        var sum = 0L

        // Pseudo-header source & destination IPs
        sum += sumWords(srcIp, 0, srcIp.size)
        sum += sumWords(dstIp, 0, dstIp.size)

        if (isIpv6) {
            sum += (udpLength ushr 16) and 0xFFFF
            sum += udpLength and 0xFFFF
            sum += PROTOCOL_UDP
        } else {
            sum += PROTOCOL_UDP
            sum += udpLength
        }

        // UDP header + payload (skipping checksum field at udpOffset + 6..7)
        var i = 0
        while (i + 1 < udpLength) {
            if (i != 6) {
                val idx = udpOffset + i
                sum += ((udpSegment[idx].toInt() and 0xFF) shl 8) or
                    (udpSegment[idx + 1].toInt() and 0xFF)
            }
            i += 2
        }
        if (udpLength % 2 != 0) {
            sum += (udpSegment[udpOffset + udpLength - 1].toInt() and 0xFF) shl 8
        }

        while ((sum ushr 16) != 0L) {
            sum = (sum and 0xFFFF) + (sum ushr 16)
        }
        val result = (sum.inv() and 0xFFFF).toInt()
        return if (result == 0) 0xFFFF else result
    }

    private fun sumWords(bytes: ByteArray, offset: Int, length: Int): Long {
        var sum = 0L
        var i = offset
        while (i + 1 < offset + length) {
            sum += ((bytes[i].toInt() and 0xFF) shl 8) or (bytes[i + 1].toInt() and 0xFF)
            i += 2
        }
        return sum
    }

    private fun readUInt16(data: ByteArray, offset: Int): Int {
        return ((data[offset].toInt() and 0xFF) shl 8) or (data[offset + 1].toInt() and 0xFF)
    }

    private fun writeUInt16(data: ByteArray, offset: Int, value: Int) {
        data[offset] = ((value ushr 8) and 0xFF).toByte()
        data[offset + 1] = (value and 0xFF).toByte()
    }
}
