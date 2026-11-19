package com.focuslock.dns.model

import java.net.InetAddress

/**
 * Dual encrypted DNS protection profiles enforced by a single local VpnService engine.
 * Never falls back silently to an unrestricted resolver.
 */
enum class DnsProfile(
    val id: String,
    val bengaliTitle: String,
    val bengaliSummary: String,
    val dohUrl: String,
    val dohHost: String,
    val bootstrapIps: List<String>
) {
    NORMAL(
        id = "normal",
        bengaliTitle = "সাধারণ মোড (সর্বদা সক্রিয়)",
        bengaliSummary = "AdGuard Default DNS — বিজ্ঞাপন ও ট্র্যাকার ব্লকিং",
        dohUrl = "https://dns.adguard-dns.com/dns-query",
        dohHost = "dns.adguard-dns.com",
        bootstrapIps = listOf(
            "94.140.14.14",
            "94.140.15.15",
            "2a10:50c0::ad1:ff",
            "2a10:50c0::ad2:ff"
        )
    ),
    FOCUS_LOCK(
        id = "focus_lock",
        bengaliTitle = "ফোকাস লক মোড",
        bengaliSummary = "AdGuard Family DNS — প্রাপ্তবয়স্ক সাইট, SafeSearch, বিজ্ঞাপন ও ট্র্যাকার ব্লক",
        dohUrl = "https://family.adguard-dns.com/dns-query",
        dohHost = "family.adguard-dns.com",
        bootstrapIps = listOf(
            "94.140.14.15",
            "94.140.15.16",
            "2a10:50c0::bad1:ff",
            "2a10:50c0::bad2:ff"
        )
    );

    fun resolveBootstrapAddresses(): List<InetAddress> =
        bootstrapIps.mapNotNull { ip ->
            runCatching { InetAddress.getByName(ip) }.getOrNull()
        }
}
