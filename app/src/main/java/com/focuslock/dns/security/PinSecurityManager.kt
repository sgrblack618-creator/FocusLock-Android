package com.focuslock.dns.security

import android.util.Base64
import com.focuslock.dns.data.FocusLockPreferences
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Protects FocusLock settings and timer changes with a salted PBKDF2-HMAC-SHA256 Admin PIN.
 * Note: An app-level PIN cannot prevent Android OS Settings VPN revocation or app uninstallation.
 */
class PinSecurityManager(
    private val preferences: FocusLockPreferences
) {
    fun isPinConfigured(): Boolean = preferences.isPinConfigured

    fun setPin(rawPin: String): Boolean {
        if (!isValidPinFormat(rawPin)) return false
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = deriveKey(rawPin, salt)

        preferences.pinSaltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        preferences.pinHashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP)
        return true
    }

    fun verifyPin(rawPin: String): Boolean {
        val saltB64 = preferences.pinSaltBase64 ?: return false
        val hashB64 = preferences.pinHashBase64 ?: return false
        if (rawPin.isBlank()) return false

        return runCatching {
            val salt = Base64.decode(saltB64, Base64.NO_WRAP)
            val expectedHash = Base64.decode(hashB64, Base64.NO_WRAP)
            val candidateHash = deriveKey(rawPin, salt)
            MessageDigest.isEqual(expectedHash, candidateHash)
        }.getOrDefault(false)
    }

    fun removePin(currentPin: String): Boolean {
        if (!verifyPin(currentPin)) return false
        preferences.clearPin()
        return true
    }

    private fun deriveKey(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }

    companion object {
        private const val SALT_BYTES = 16
        private const val KEY_BITS = 256
        private const val PBKDF2_ITERATIONS = 65_536

        fun isValidPinFormat(pin: String): Boolean =
            pin.length in 4..8 && pin.all { it.isDigit() }
    }
}
