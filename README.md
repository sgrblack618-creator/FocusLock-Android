# FocusLock — Native Android DNS Content & Ad Filtering App

Target Device: **ZTE Blade A54 (Android 13 / MyOS 13, 4GB RAM)**
Minimum SDK: **26 (Android 8.0)** · Target SDK: **34** · Tech Stack: **Kotlin + Jetpack Compose + VpnService + OkHttp DoH**

## Dual DNS Protection Profiles

1. **Normal Mode (Always-On)**
   - Endpoint: `https://dns.adguard-dns.com/dns-query` (AdGuard Default DNS)
   - Blocks ads & trackers at all times.
2. **Focus Lock Mode (Timed)**
   - Endpoint: `https://family.adguard-dns.com/dns-query` (AdGuard Family DNS)
   - Blocks adult websites (e.g., xHamster and adult domains), enforces SafeSearch where supported, and blocks ads & trackers.
   - Automatically reverts to **Normal Mode** when the timer expires — never disabling ad blocking.

## Building the Installable APK

### Option A: Android Studio / Local CLI (Requires JDK 17 + Android SDK)
1. Open the `FocusLock-Android` folder in **Android Studio** (or install Android SDK Build-Tools 34 & Platform 34).
2. Let Gradle sync dependencies (`gradle-8.9`).
3. Build a debug or release APK:
   ```bash
   gradle assembleDebug
   # Or optimized R8-shrunk release APK (ARM64 + ARMv7):
   gradle assembleRelease
   ```
4. Install onto your ZTE Blade A54 via USB debugging:
   ```bash
   adb install -r app/build/outputs/apk/release/app-release.apk
   ```

### Option B: Cloud APK Build via GitHub Actions (No PC Required)
1. Upload this `FocusLock-Android` folder to a GitHub repository.
2. Open the **Actions** tab and run **Build FocusLock APK** (`.github/workflows/build-apk.yml`).
3. Download the `FocusLock-APKs` artifact ZIP and install the `.apk` directly on your ZTE Blade A54.

## Important Honest Disclosures & Limitations
- **DNS Bypass Methods**: DNS filtering cannot block every possible bypass (e.g. Chrome "Secure DNS" / browser-level DoH, external VPN apps, or direct IP addresses). Disable browser Secure DNS and set Android System *Private DNS* to Off/Automatic.
- **PIN Protection Scope**: The PBKDF2-HMAC-SHA256 Admin PIN protects in-app timer & filtering settings, but an ordinary Android app PIN cannot stop Android OS System Settings VPN revocation or app uninstallation.
- **Privacy**: Zero browsing history, zero domain logging, zero HTTPS payload inspection, and zero analytics.
