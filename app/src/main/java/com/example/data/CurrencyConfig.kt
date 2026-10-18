package com.example.data

data class CurrencyItem(
    val code: String,
    val nameAr: String,
    val nameEn: String,
    val symbolAr: String,
    val symbolEn: String,
    val flagEmoji: String
)

object CurrencyConfig {

    val ALL_CURRENCIES = listOf(
        CurrencyItem("EGP", "جنيه مصري", "Egyptian Pound", "ج.م", "EGP", "🇪🇬"),
        CurrencyItem("SAR", "ريال سعودي", "Saudi Riyal", "ر.س", "SAR", "🇸🇦"),
        CurrencyItem("AED", "درهم إماراتي", "UAE Dirham", "د.إ", "AED", "🇦🇪"),
        CurrencyItem("KWD", "دينار كويتي", "Kuwaiti Dinar", "د.ك", "KWD", "🇰🇼"),
        CurrencyItem("QAR", "ريال قطري", "Qatari Riyal", "ر.ق", "QAR", "🇶🇦"),
        CurrencyItem("BHD", "دينار بحريني", "Bahraini Dinar", "د.ب", "BHD", "🇧🇭"),
        CurrencyItem("OMR", "ريال عماني", "Omani Rial", "ر.ع", "OMR", "🇴🇲"),
        CurrencyItem("JOD", "دينار أردني", "Jordanian Dinar", "د.أ", "JOD", "🇯🇴"),
        CurrencyItem("IQD", "دينار عراقي", "Iraqi Dinar", "د.ع", "IQD", "🇮🇶"),
        CurrencyItem("MAD", "درهم مغربي", "Moroccan Dirham", "د.م", "MAD", "🇲🇦"),
        CurrencyItem("DZD", "دينار جزائري", "Algerian Dinar", "د.ج", "DZD", "🇩🇿"),
        CurrencyItem("TND", "دينار تونسي", "Tunisian Dinar", "د.ت", "TND", "🇹🇳"),
        CurrencyItem("LYD", "دينار ليبي", "Libyan Dinar", "د.ل", "LYD", "🇱🇾"),
        CurrencyItem("SDG", "جنيه سوداني", "Sudanese Pound", "ج.س", "SDG", "🇸🇩"),
        CurrencyItem("LBP", "ليرة لبنانية", "Lebanese Pound", "ل.ل", "LBP", "🇱🇧"),
        CurrencyItem("SYP", "ليرة سورية", "Syrian Pound", "ل.س", "SYP", "🇸🇾"),
        CurrencyItem("YER", "ريال يمني", "Yemeni Rial", "ر.ي", "YER", "🇾🇪"),
        CurrencyItem("ILS", "شيكل فلسطيني", "Palestinian Shekel", "₪", "ILS", "🇵🇸"),
        CurrencyItem("MRU", "أوقية موريتانية", "Mauritanian Ouguiya", "أ.م", "MRU", "🇲🇷"),
        CurrencyItem("DJF", "فرنك جيبوتي", "Djiboutian Franc", "ف.ج", "DJF", "🇩🇯"),
        CurrencyItem("KMF", "فرنك قمري", "Comorian Franc", "ف.ق", "KMF", "🇰🇲"),
        CurrencyItem("SOS", "شلن صومالي", "Somali Shilling", "ش.ص", "SOS", "🇸🇴"),
        CurrencyItem("USD", "دولار أمريكي", "US Dollar", "$", "$", "🇺🇸")
    )

    fun getCurrency(code: String): CurrencyItem {
        return ALL_CURRENCIES.find { it.code.equals(code, ignoreCase = true) }
            ?: ALL_CURRENCIES[0] // Default to EGP
    }

    fun getSymbol(code: String, isArabic: Boolean = true): String {
        val item = getCurrency(code)
        return if (isArabic) item.symbolAr else item.symbolEn
    }

    fun format(
        amount: Double,
        currencyCode: String = "EGP",
        isArabic: Boolean = true,
        showDecimals: Boolean = false
    ): String {
        val symbol = getSymbol(currencyCode, isArabic)
        val formattedNumber = if (showDecimals || amount % 1.0 != 0.0) {
            String.format(java.util.Locale.US, "%.2f", amount)
        } else {
            String.format(java.util.Locale.US, "%,d", amount.toLong())
        }
        return if (isArabic) {
            "$formattedNumber $symbol"
        } else {
            if (symbol == "$") "$symbol$formattedNumber" else "$formattedNumber $symbol"
        }
    }
}
