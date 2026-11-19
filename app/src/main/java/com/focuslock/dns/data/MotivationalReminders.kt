package com.focuslock.dns.data

import androidx.annotation.DrawableRes
import com.focuslock.dns.R
import java.util.Locale

data class MotivationalQuote(
    val titleEn: String,
    val bodyEn: String,
    val arabicLine: String? = null,
    val sourceEn: String,
    val titleBn: String,
    val bodyBn: String,
    val sourceBn: String,
    @DrawableRes val imageResId: Int? = null
)

/**
 * Curated Islamic & self-discipline reminders featuring the user's chosen quotes and backdrops.
 * Zero browsing history, zero domain strings, and zero logs are stored.
 */
object MotivationalReminders {

    val quotes: List<MotivationalQuote> = listOf(
        MotivationalQuote(
            titleEn = "It's time to show my best version once again.",
            bodyEn = "Every urge you overcome makes your discipline stronger. Do not trade your dignity and peace for a fleeting distraction.",
            arabicLine = null,
            sourceEn = "Personal Resolve · Best Version",
            titleBn = "আবারও নিজের সেরা রূপটি দেখানোর সময় এসেছে",
            bodyBn = "প্রতিটি সংযম আপনার মানসিক শক্তি বাড়ায়। ক্ষণিকের প্রবৃত্তির কাছে নিজের মর্যাদা ও প্রশান্তি হারাবেন না।",
            sourceBn = "আত্মবিশ্বাস · সেরা সংস্করণ",
            imageResId = R.drawable.quote_best_version
        ),
        MotivationalQuote(
            titleEn = "SUCCESS — Come to prayer, Come to success",
            bodyEn = "People say they want success in this life. But when they hear the adhan — 'Come to prayer, Come to success' — they ignore it.",
            arabicLine = "حَيَّ عَلَى الصَّلَاةِ حَيَّ عَلَى الْفَلَاحِ",
            sourceEn = "Call of the Adhan · True Success",
            titleBn = "সফলতা — এসো নামাজের দিকে, এসো সফলতার দিকে",
            bodyBn = "মানুষ বলে তারা দুনিয়ায় সফলতা চায়। কিন্তু যখন আযানে ডাকা হয় — 'এসো নামাজের দিকে, এসো সফলতার দিকে' — তখন তারা তা উপেক্ষা করে।",
            sourceBn = "আযানের আহ্বান · প্রকৃত সাফল্য",
            imageResId = R.drawable.quote_adhan_success
        ),
        MotivationalQuote(
            titleEn = "Allah Sees Everything",
            bodyEn = "Does he not know that Allah sees? Guard your eyes in private just as you guard them in public.",
            arabicLine = "أَلَمْ يَعْلَم بِأَنَّ اللَّهَ يَرَىٰ",
            sourceEn = "Surah Al-Alaq (96:14)",
            titleBn = "নিশ্চয়ই আল্লাহ সব দেখছেন",
            bodyBn = "সে কি জানে না যে আল্লাহ সবকিছু দেখেন? একান্ত গোপনেও নিজের দৃষ্টি ও অন্তরকে পবিত্র রাখুন।",
            sourceBn = "সুরা আল-আলাক (৯৬:১৪)",
            imageResId = R.drawable.quote_adhan_success
        ),
        MotivationalQuote(
            titleEn = "Protect Your Gaze, Protect Your Heart",
            bodyEn = "Tell the believing men to lower their gaze and guard their chastity. That is purer for them.",
            arabicLine = null,
            sourceEn = "Surah An-Nur (24:30)",
            titleBn = "দৃষ্টি সংযত রাখুন, হৃদয় প্রশান্ত হবে",
            bodyBn = "মুমিনদের বলুন, তারা যেন তাদের দৃষ্টি নত রাখে এবং লজ্জাস্থানের হেফাজত করে। এটি তাদের জন্য অধিক পবিত্র।",
            sourceBn = "সুরা আন-নূর (২৪:৩০)",
            imageResId = R.drawable.quote_best_version
        ),
        MotivationalQuote(
            titleEn = "Leave It for Allah's Sake",
            bodyEn = "Whoever gives up something for the sake of Allah, Allah will replace it with something far better and more peaceful.",
            arabicLine = null,
            sourceEn = "Prophetic Wisdom · Self-Control",
            titleBn = "আল্লাহর সন্তুষ্টির জন্য ছেড়ে দিন",
            bodyBn = "যে ব্যক্তি আল্লাহর ভয়ে কোনো খারাপ কাজ ত্যাগ করে, আল্লাহ তার জন্য এর চেয়ে উত্তম ও প্রশান্তিময় কিছু দান করেন।",
            sourceBn = "হাদিসের শিক্ষা · আত্মনিয়ন্ত্রণ",
            imageResId = R.drawable.quote_adhan_success
        )
    )

    fun quoteForIndex(index: Int): MotivationalQuote {
        val safeIdx = ((index % quotes.size) + quotes.size) % quotes.size
        return quotes[safeIdx]
    }

    /**
     * Checks purely in RAM whether a DNS wire-format response indicates a blocked domain:
     * - RCODE == 3 (NXDOMAIN)
     * - OR an A record pointing to 0.0.0.0
     * - OR an AAAA record pointing to ::
     */
    fun isBlockedDnsResponse(dnsResponse: ByteArray): Boolean {
        if (dnsResponse.size < 12) return false
        val rcode = dnsResponse[3].toInt() and 0x0F
        if (rcode == 3) return true

        val anCount = ((dnsResponse[6].toInt() and 0xFF) shl 8) or (dnsResponse[7].toInt() and 0xFF)
        if (anCount == 0) return false

        var offset = 12
        val qdCount = ((dnsResponse[4].toInt() and 0xFF) shl 8) or (dnsResponse[5].toInt() and 0xFF)
        for (q in 0 until qdCount) {
            offset = skipDnsName(dnsResponse, offset) ?: return false
            offset += 4
            if (offset > dnsResponse.size) return false
        }

        for (a in 0 until anCount) {
            offset = skipDnsName(dnsResponse, offset) ?: return false
            if (offset + 10 > dnsResponse.size) return false
            val rrType = ((dnsResponse[offset].toInt() and 0xFF) shl 8) or
                (dnsResponse[offset + 1].toInt() and 0xFF)
            val rdLength = ((dnsResponse[offset + 8].toInt() and 0xFF) shl 8) or
                (dnsResponse[offset + 9].toInt() and 0xFF)
            offset += 10
            if (offset + rdLength > dnsResponse.size) return false

            if (rrType == 1 && rdLength == 4) {
                if (dnsResponse[offset] == 0.toByte() &&
                    dnsResponse[offset + 1] == 0.toByte() &&
                    dnsResponse[offset + 2] == 0.toByte() &&
                    dnsResponse[offset + 3] == 0.toByte()
                ) {
                    return true
                }
            } else if (rrType == 28 && rdLength == 16) {
                var allZero = true
                for (i in 0 until 16) {
                    if (dnsResponse[offset + i] != 0.toByte()) {
                        allZero = false
                        break
                    }
                }
                if (allZero) return true
            }
            offset += rdLength
        }
        return false
    }

    /**
     * Transiently checks the queried QNAME in RAM only (never logged or persisted)
     * so motivational popups fire for adult-domain blocks rather than background ad trackers.
     */
    fun isAdultCategoryQueryInRam(dnsQuery: ByteArray): Boolean {
        val qname = extractQnameTransiently(dnsQuery) ?: return false
        return ADULT_KEYWORDS.any { keyword -> qname.contains(keyword) }
    }

    private fun extractQnameTransiently(dnsPacket: ByteArray): String? {
        if (dnsPacket.size < 13) return null
        val sb = StringBuilder(48)
        var offset = 12
        var guard = 0
        while (offset < dnsPacket.size && guard++ < 64) {
            val len = dnsPacket[offset].toInt() and 0xFF
            if (len == 0) break
            if ((len and 0xC0) != 0) break
            offset++
            if (offset + len > dnsPacket.size) return null
            if (sb.isNotEmpty()) sb.append('.')
            for (i in 0 until len) {
                sb.append((dnsPacket[offset + i].toInt() and 0xFF).toChar())
            }
            offset += len
        }
        return sb.toString().lowercase(Locale.ROOT)
    }

    private fun skipDnsName(packet: ByteArray, startOffset: Int): Int? {
        var offset = startOffset
        var guard = 0
        while (offset < packet.size && guard++ < 64) {
            val len = packet[offset].toInt() and 0xFF
            if (len == 0) return offset + 1
            if ((len and 0xC0) == 0xC0) {
                return if (offset + 2 <= packet.size) offset + 2 else null
            }
            offset += 1 + len
        }
        return null
    }

    private val ADULT_KEYWORDS = listOf(
        "xhamster", "pornhub", "xvideos", "xnxx", "redtube", "youporn",
        "tube8", "spankbang", "brazzers", "bangbros", "naughtyamerica",
        "onlyfans", "chaturbate", "stripchat", "bongacams", "cam4",
        "livejasmin", "eporner", "hqporner", "beeg", "tnaflix",
        "youjizz", "hentai", "rule34", "nhentai", "hanime", "porntrex",
        "txxx", "vporn", "drtuber", "sunporno", "porn"
    )
}
