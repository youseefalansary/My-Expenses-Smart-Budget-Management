package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ThemeMode(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val isDark: Boolean
) {
    DARK_SLATE(
        id = "dark_slate",
        titleAr = "دارك سليت ونيون",
        subtitleAr = "أزرق ليلي غامق مع لمسات نيون سايان",
        isDark = true
    ),
    PURE_LIGHT(
        id = "pure_light",
        titleAr = "أبيض ناصع (فاتح)",
        subtitleAr = "واجهة بيضاء نقية ومريحة للعين في النهار",
        isDark = false
    ),
    EMERALD_GOLD(
        id = "emerald_gold",
        titleAr = "الزمردي الملكي والذهب",
        subtitleAr = "درجات الأخضر الزمردي مع لمسات ذهبية فخمة",
        isDark = true
    ),
    MIDNIGHT_OLED(
        id = "midnight_oled",
        titleAr = "سواد مطلق OLED",
        subtitleAr = "أسود فاحم نقي 100% لتوفير البطارية",
        isDark = true
    );

    companion object {
        fun fromId(id: String): ThemeMode {
            return entries.find { it.id == id } ?: DARK_SLATE
        }
    }
}

enum class CornerStyle(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val radiusDp: Int
) {
    CLASSIC(
        id = "classic",
        titleAr = "دائري متوازن (20px)",
        subtitleAr = "الحواف المنحنية الفاخرة المعتمدة للتطبيق",
        radiusDp = 20
    ),
    SHARP(
        id = "sharp",
        titleAr = "تصميم حاد هندسي",
        subtitleAr = "حواف مستقيمة تقنية وبسيطة (4px)",
        radiusDp = 4
    ),
    SMOOTH(
        id = "smooth",
        titleAr = "مربع بحواف ناعمة",
        subtitleAr = "تصميم سكوايركل ناعم وعصري (14px)",
        radiusDp = 14
    ),
    PILL(
        id = "pill",
        titleAr = "دائري فائق (كبسولة)",
        subtitleAr = "حواف دائرية للغاية بشكل بيضاوي أنيق (28px)",
        radiusDp = 28
    );

    companion object {
        fun fromId(id: String): CornerStyle {
            return entries.find { it.id == id } ?: CLASSIC
        }
    }
}

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("masrofi_theme_prefs", Context.MODE_PRIVATE)

    fun getThemeMode(): ThemeMode {
        val id = prefs.getString("theme_mode", ThemeMode.DARK_SLATE.id) ?: ThemeMode.DARK_SLATE.id
        return ThemeMode.fromId(id)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.id).apply()
    }

    fun getCornerStyle(): CornerStyle {
        val id = prefs.getString("corner_style", CornerStyle.CLASSIC.id) ?: CornerStyle.CLASSIC.id
        return CornerStyle.fromId(id)
    }

    fun setCornerStyle(style: CornerStyle) {
        prefs.edit().putString("corner_style", style.id).apply()
    }

    fun getLanguage(): String {
        return prefs.getString("app_language", "ar") ?: "ar"
    }

    fun setLanguage(languageCode: String) {
        prefs.edit().putString("app_language", languageCode).apply()
    }

    fun getCurrency(): String {
        return prefs.getString("app_currency", "EGP") ?: "EGP"
    }

    fun setCurrency(currencyCode: String) {
        prefs.edit().putString("app_currency", currencyCode).apply()
    }
}

val LocalAppCornerRadius = compositionLocalOf { 20.dp }
val LocalThemeMode = compositionLocalOf { ThemeMode.DARK_SLATE }
val LocalAppCurrency = compositionLocalOf { "EGP" }
val LocalAppLanguage = compositionLocalOf { "ar" }
