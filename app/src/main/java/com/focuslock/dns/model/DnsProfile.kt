package com.focuslock.dns.model

import java.net.InetAddress

/**
 * Encrypted DNS protection profiles enforced by a single local VpnService engine.
 * Supports toggling regular Ad Blocking ON/OFF anytime without disabling Adult/Malware filtering
 * and never falls back silently to an unrestricted plaintext DNS resolver.
 */
enum class DnsProfile(
    val id: String,
    val isFocusLockAdultFilter: Boolean,
    val isAdBlockEnabled: Boolean,
    val dohUrl: String,
    val dohHost: String,
    val bootstrapIps: List<String>
) {
    NORMAL(
        id = "normal",
        isFocusLockAdultFilter = false,
        isAdBlockEnabled = true,
        dohUrl = "https://dns.adguard-dns.com/dns-query",
        dohHost = "dns.adguard-dns.com",
        bootstrapIps = listOf(
            "94.140.14.14",
            "94.140.15.15",
            "2a10:50c0::ad1:ff",
            "2a10:50c0::ad2:ff"
        )
    ),
    NORMAL_NO_ADBLOCK(
        id = "normal_no_adblock",
        isFocusLockAdultFilter = false,
        isAdBlockEnabled = false,
        dohUrl = "https://security.cloudflare-dns.com/dns-query",
        dohHost = "security.cloudflare-dns.com",
        bootstrapIps = listOf(
            "1.1.1.2",
            "1.0.0.2",
            "2606:4700:4700::1112",
            "2606:4700:4700::1002"
        )
    ),
    FOCUS_LOCK(
        id = "focus_lock",
        isFocusLockAdultFilter = true,
        isAdBlockEnabled = true,
        dohUrl = "https://family.adguard-dns.com/dns-query",
        dohHost = "family.adguard-dns.com",
        bootstrapIps = listOf(
            "94.140.14.15",
            "94.140.15.16",
            "2a10:50c0::bad1:ff",
            "2a10:50c0::bad2:ff"
        )
    ),
    FOCUS_LOCK_NO_ADBLOCK(
        id = "focus_lock_no_adblock",
        isFocusLockAdultFilter = true,
        isAdBlockEnabled = false,
        dohUrl = "https://family.cloudflare-dns.com/dns-query",
        dohHost = "family.cloudflare-dns.com",
        bootstrapIps = listOf(
            "1.1.1.3",
            "1.0.0.3",
            "2606:4700:4700::1113",
            "2606:4700:4700::1003"
        )
    );

    fun resolveBootstrapAddresses(): List<InetAddress> =
        bootstrapIps.mapNotNull { ip ->
            runCatching { InetAddress.getByName(ip) }.getOrNull()
        }
}
