package com.focuslock.dns.vpn

import android.net.VpnService
import com.focuslock.dns.model.DnsProfile
import okhttp3.ConnectionPool
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.SocketFactory

/**
 * Encrypted DNS-over-HTTPS (RFC 8484) client.
 * - Uses strict Android system TLS certificate & hostname verification.
 * - Protects upstream sockets via VpnService.protect() to prevent routing loops.
 * - Resolves DoH hostnames via static AdGuard Anycast bootstrap IPs (no DNS bootstrap loop).
 * - Fail-closed: never falls back to unrestricted plaintext DNS.
 */
class DohResolverClient(vpnService: VpnService) {

    private val dohMediaType = "application/dns-message".toMediaType()

    private val bootstrapDns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> =
            DnsProfile.entries.firstOrNull { it.dohHost == hostname }?.resolveBootstrapAddresses()
                ?: throw UnknownHostException("Blocked non-DoH hostname resolution: $hostname")
    }

    private val protectedSocketFactory = object : SocketFactory() {
        private val delegate = getDefault()

        private fun protect(socket: Socket): Socket {
            vpnService.protect(socket)
            return socket
        }

        override fun createSocket(): Socket = protect(delegate.createSocket())

        override fun createSocket(host: String?, port: Int): Socket =
            protect(delegate.createSocket(host, port))

        override fun createSocket(
            host: String?,
            port: Int,
            localHost: InetAddress?,
            localPort: Int
        ): Socket = protect(delegate.createSocket(host, port, localHost, localPort))

        override fun createSocket(host: InetAddress?, port: Int): Socket =
            protect(delegate.createSocket(host, port))

        override fun createSocket(
            address: InetAddress?,
            port: Int,
            localAddress: InetAddress?,
            localPort: Int
        ): Socket = protect(delegate.createSocket(address, port, localAddress, localPort))
    }

    private val dispatcher = okhttp3.Dispatcher().apply {
        maxRequests = 16
        maxRequestsPerHost = 8
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .dispatcher(dispatcher)
        .dns(bootstrapDns)
        .socketFactory(protectedSocketFactory)
        .connectionPool(ConnectionPool(2, 60, TimeUnit.SECONDS))
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(4, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    fun resolveDnsQuery(profile: DnsProfile, dnsWireQuery: ByteArray): ByteArray? {
        val request = Request.Builder()
            .url(profile.dohUrl)
            .header("Accept", "application/dns-message")
            .post(dnsWireQuery.toRequestBody(dohMediaType))
            .build()

        return runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return null
                }
                val bytes = response.body?.bytes() ?: return null
                if (bytes.size < 12) null else bytes
            }
        }.getOrNull()
    }

    fun shutdown() {
        runCatching {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
        }
    }
}
