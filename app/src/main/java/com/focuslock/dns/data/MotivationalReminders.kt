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
            titleEn = "So, surely with hardship comes ease.",
            bodyEn = "Verily, with every hardship comes more ease. Stand firm right now—relief and strength follow patience.",
            arabicLine = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا ۝ إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            sourceEn = "Surah Ash-Sharh (94:5-6)",
            titleBn = "নিশ্চয়ই কষ্টের সাথেই রয়েছে স্বস্তি",
            bodyBn = "প্রকৃতপক্ষে প্রতিটি কঠিন পরীক্ষার পরই আসে সহজতা ও প্রশান্তি। এই মুহূর্তের ধৈর্যই আপনার বিজয়।",
            sourceBn = "সুরা আল-ইনশিরাহ (৯৪:৫-৬)",
            imageResId = R.drawable.quote_adhan_success
        ),
        MotivationalQuote(
            titleEn = "It's You vs You.",
            bodyEn = "No one else has to fight this quiet battle for you. Every time you walk away from temptation, your stronger self wins.",
            arabicLine = null,
            sourceEn = "Inner Discipline · Daily Battle",
            titleBn = "লড়াইটা শুধু তোমার নিজের সাথে নিজের",
            bodyBn = "এই নীরব যুদ্ধটা আপনাকেই জিততে হবে। প্রতিটি প্রলোভনকে না বলার মাধ্যমেই আপনার শক্তিশালী রূপ জয়ী হয়।",
            sourceBn = "আত্মসংযম · প্রতিদিন নিজেকে জয়",
            imageResId = R.drawable.quote_best_version
        ),
        MotivationalQuote(
            titleEn = "Do not be afraid; Allah is listening and seeing.",
            bodyEn = "Do not fear being weak or alone in your struggle. Allah is with you at every moment, hearing and seeing your effort.",
            arabicLine = "قَالَ لَا تَخَافَا ۖ إِنَّنِي مَعَكُمَا أَسْمَعُ وَأَرَىٰ",
            sourceEn = "Surah Taha (20:46)",
            titleBn = "ভয় পেয়ো না; আমি তোমাদের সাথে আছি, সব শুনি ও দেখি",
            bodyBn = "সংগ্রামে নিজেকে একা ভাববেন না। আপনার প্রতিটি ভালো চেষ্টা আল্লাহ দেখছেন এবং শুনছেন।",
            sourceBn = "সুরা ত্বহা (২০:৪৬)",
            imageResId = R.drawable.quote_adhan_success
        ),
        MotivationalQuote(
            titleEn = "Remember the goal. The struggle is temporary, the result is forever.",
            bodyEn = "A moment of urge passes quickly, but a clean conscience, strong character, and self-respect last.",
            arabicLine = null,
            sourceEn = "Long-Term Vision · Stay Locked",
            titleBn = "লক্ষ্যটা মনে রাখো — কষ্ট সাময়িক, কিন্তু সাফল্য চিরস্থায়ী",
            bodyBn = "ক্ষণিকের প্রবৃত্তি কয়েক মিনিটেই চলে যায়, কিন্তু পবিত্র হৃদয় ও আত্মসম্মান সারাজীবন থাকে।",
            sourceBn = "দৃঢ় লক্ষ্য · স্থির থাকুন",
            imageResId = R.drawable.quote_best_version
        ),
        MotivationalQuote(
            titleEn = "And seek help through patience and prayer.",
            bodyEn = "Whenever urges feel heavy, turn immediately to Sabr and Salah—true peace begins on the prayer mat.",
            arabicLine = "وَاسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ",
            sourceEn = "Surah Al-Baqarah (2:45)",
            titleBn = "ধৈর্য ও নামাজের মাধ্যমে সাহায্য প্রার্থনা করো",
            bodyBn = "মন অস্থির হলেই ধৈর্য ধরুন এবং নামাজে দাঁড়িয়ে যান—প্রকৃত মানসিক শক্তি সেখান থেকেই আসে।",
            sourceBn = "সুরা আল-বাকারা (২:৪৫)",
            imageResId = R.drawable.quote_adhan_success
        ),
        MotivationalQuote(
            titleEn = "Success begins when giving up is no longer an option.",
            bodyEn = "You locked your device because you chose growth over excuses. Stand by the promise you made to yourself.",
            arabicLine = null,
            sourceEn = "No Excuses · Pure Focus",
            titleBn = "সফলতা শুরু হয় যখন হার মানা আর কোনো বিকল্প থাকে না",
            bodyBn = "আপনি অজুহাতের বদলে উন্নতি বেছে নিয়েছেন। নিজের কাছে করা অঙ্গীকারে অটল থাকুন।",
            sourceBn = "অটল সংকল্প · ফোকাস",
            imageResId = R.drawable.quote_best_version
        ),
        MotivationalQuote(
            titleEn = "Don't be sad, Allah is with us.",
            bodyEn = "Even if you slipped in the past, do not despair. Turn back sincerely right now—Allah is always near.",
            arabicLine = "لَا تَحْزَنْ إِنَّ اللَّهَ مَعَنَا",
            sourceEn = "Surah At-Tawbah (9:40)",
            titleBn = "হতাশ হয়ো না, নিশ্চয়ই আল্লাহ আমাদের সাথে আছেন",
            bodyBn = "অতীতের ভুল নিয়ে ভেঙে পড়বেন না। এখনই ফিরে আসুন, আল্লাহর রহমত ও সাহায্য আপনার সাথেই আছে।",
            sourceBn = "সুরা আত-তাওবাহ (৯:৪০)",
            imageResId = R.drawable.quote_adhan_success
        ),
        MotivationalQuote(
            titleEn = "If Allah helps you, none can defeat you.",
            bodyEn = "Ask Allah for strength over your desires; with His help, no addiction or habit can conquer you.",
            arabicLine = "إِن يَنصُرْكُمُ اللَّهُ فَلَا غَالِبَ لَكُمْ",
            sourceEn = "Surah Ali 'Imran (3:160)",
            titleBn = "আল্লাহ তোমাকে সাহায্য করলে কেউ তোমাকে পরাজিত করতে পারবে না",
            bodyBn = "নিজের প্রবৃত্তির ওপর বিজয়ী হতে আল্লাহর সাহায্য চান—তাঁর সাহায্যে কোনো বদভ্যাস আপনাকে হারাতে পারবে না।",
            sourceBn = "সুরা আল-ইমরান (৩:১৬০)",
            imageResId = R.drawable.quote_adhan_success
        ),
        MotivationalQuote(
            titleEn = "And your Lord never forgets.",
            bodyEn = "Every tear you hold back, every bad page you close, and every prayer you whisper is remembered by your Lord.",
            arabicLine = "وَمَا كَانَ رَبُّكَ نَسِيًّا",
            sourceEn = "Surah Maryam (19:64)",
            titleBn = "আর তোমার রব কখনোই ভুলে যান না",
            bodyBn = "আপনি আল্লাহর ভয়ে যে প্রতিটি খারাপ জিনিস বর্জন করেন, আপনার রব তার সবই মনে রাখেন ও পুরস্কৃত করেন।",
            sourceBn = "সুরা মারইয়াম (১৯:৬৪)",
            imageResId = R.drawable.quote_adhan_success
        ),
        MotivationalQuote(
            titleEn = "Remember someone with the same goal is working harder than you.",
            bodyEn = "The price of progress is pain—keep working through bad days and good days, and try again and again.",
            arabicLine = null,
            sourceEn = "Relentless Consistency",
            titleBn = "মনে রেখো, তোমার মতোই লক্ষ্য নিয়ে কেউ আরও কঠোর পরিশ্রম করছে",
            bodyBn = "উন্নতির মূল্য হলো ধৈর্য ও কষ্ট সহ্য করা। ভালো দিন হোক বা কঠিন দিন—চেষ্টা চালিয়ে যাও।",
            sourceBn = "নিরলস চেষ্টা · অগ্রগতি",
            imageResId = R.drawable.quote_best_version
        ),
        MotivationalQuote(
            titleEn = "Whoever puts their trust in Allah, He is sufficient for them.",
            bodyEn = "Place full reliance on Allah and close the door to what harms your heart. He alone is enough for you.",
            arabicLine = "وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ",
            sourceEn = "Surah At-Talaq (65:3)",
            titleBn = "যে আল্লাহর ওপর ভরসা করে, তিনিই তার জন্য যথেষ্ট",
            bodyBn = "অন্তর নষ্ট করে এমন সব পথ বন্ধ করে আল্লাহর ওপর পূর্ণ ভরসা রাখুন—তিনিই আপনার জন্য যথেষ্ট।",
            sourceBn = "সুরা আত-তালাক (৬৫:৩)",
            imageResId = R.drawable.quote_adhan_success
        ),
        MotivationalQuote(
            titleEn = "True honor comes from the Most Merciful.",
            bodyEn = "He who seeks honor in desires or people is humbled; he who seeks honor with Ar-Rahman remains forever dignified.",
            arabicLine = "مَنْ عَزَّ بِالرَّحْمَنِ ظَلَّ عَزِيزًا",
            sourceEn = "Islamic Wisdom · Dignity",
            titleBn = "যে পরম দয়ালু আল্লাহর মাধ্যমে সম্মান চায়, সে চিরসম্মানিত থাকে",
            bodyBn = "দুনিয়ার ক্ষণিক মোহে সম্মান নেই; প্রকৃত মর্যাদা কেবল আল্লাহর আনুগত্য ও আত্মসম্মানের ভেতরেই।",
            sourceBn = "ইসলামিক প্রজ্ঞা · আত্মমর্যাদা",
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

    fun hourlyQuoteIndex(
        nowEpochMs: Long = System.currentTimeMillis(),
        offset: Int = 0
    ): Int {
        val hourBucket = (nowEpochMs / 3_600_000L).toInt()
        val combined = hourBucket + offset
        return ((combined % quotes.size) + quotes.size) % quotes.size
    }

    fun hourlyQuote(
        nowEpochMs: Long = System.currentTimeMillis(),
        offset: Int = 0
    ): MotivationalQuote = quoteForIndex(hourlyQuoteIndex(nowEpochMs, offset))

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

    /**
     * Checks purely in RAM (never logged or persisted) whether a queried domain matches
     * user-enabled Gambling/Betting or Social Distraction shields.
     */
    fun shouldBlockByLocalRuleInRam(
        dnsQuery: ByteArray,
        blockGambling: Boolean,
        blockSocial: Boolean
    ): Boolean {
        if (!blockGambling && !blockSocial) return false
        val qname = extractQnameTransiently(dnsQuery) ?: return false
        if (blockGambling && matchesDomainOrKeyword(qname, GAMBLING_DOMAINS)) {
            return true
        }
        if (blockSocial && matchesDomainOrSuffix(qname, SOCIAL_DOMAINS)) {
            return true
        }
        return false
    }

    private fun matchesDomainOrSuffix(qname: String, domains: List<String>): Boolean {
        return domains.any { domain -> qname == domain || qname.endsWith(".$domain") }
    }

    private fun matchesDomainOrKeyword(qname: String, keywords: List<String>): Boolean {
        return keywords.any { kw -> qname == kw || qname.endsWith(".$kw") || qname.contains(kw) }
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

    private val GAMBLING_DOMAINS = listOf(
        "1xbet", "melbet", "mostbet", "linebet", "betwinner", "parimatch",
        "bet365", "betway", "22bet", "megapari", "stake.com", "betfair",
        "888casino", "pin-up", "bwin.com", "draftkings", "fanduel", "baji999",
        "jeebuzz", "crickex"
    )

    private val SOCIAL_DOMAINS = listOf(
        "tiktok.com", "tiktokcdn.com", "tiktokv.com", "byteoversea.com",
        "reddit.com", "redd.it", "redditmedia.com",
        "x.com", "twitter.com", "twimg.com"
    )

    private val ADULT_KEYWORDS = listOf(
        "xhamster", "pornhub", "xvideos", "xnxx", "redtube", "youporn",
        "tube8", "spankbang", "brazzers", "bangbros", "naughtyamerica",
        "onlyfans", "chaturbate", "stripchat", "bongacams", "cam4",
        "livejasmin", "eporner", "hqporner", "beeg", "tnaflix",
        "youjizz", "hentai", "rule34", "nhentai", "hanime", "porntrex",
        "txxx", "vporn", "drtuber", "sunporno", "porn"
    )
}
