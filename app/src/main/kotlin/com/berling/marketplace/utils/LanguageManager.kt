package com.berling.marketplace.utils

import android.content.Context
import java.util.*

/**
 * LanguageManager handles multi-language support for the marketplace
 * Supports all device languages and allows users to override the system language
 */
object LanguageManager {

    private const val LANGUAGE_PREF = "selected_language"
    private const val LANGUAGE_PREF_FILE = "language_prefs"

    // Supported languages
    val SUPPORTED_LANGUAGES = mapOf(
        "en" to "English",
        "es" to "Español",
        "fr" to "Français",
        "de" to "Deutsch",
        "it" to "Italiano",
        "pt" to "Português",
        "ru" to "Русский",
        "ja" to "日本語",
        "zh" to "中文",
        "ar" to "العربية",
        "hi" to "हिन्दी",
        "ko" to "한국어",
        "tr" to "Türkçe",
        "pl" to "Polski",
        "nl" to "Nederlands",
        "sv" to "Svenska",
        "da" to "Dansk",
        "fi" to "Suomi",
        "no" to "Norsk",
        "th" to "ไทย",
        "id" to "Bahasa Indonesia",
        "vi" to "Tiếng Việt",
        "ms" to "Bahasa Melayu",
        "tl" to "Tagalog",
        "uk" to "Українська"
    )

    /**
     * Get the current language code
     */
    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(LANGUAGE_PREF_FILE, Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString(LANGUAGE_PREF, null)
        
        return savedLanguage ?: getDeviceLanguage()
    }

    /**
     * Get device's system language
     */
    fun getDeviceLanguage(): String {
        val locale = Locale.getDefault()
        val language = locale.language
        
        return if (SUPPORTED_LANGUAGES.containsKey(language)) {
            language
        } else {
            "en" // Default to English
        }
    }

    /**
     * Set the user's preferred language
     */
    fun setLanguage(context: Context, languageCode: String) {
        if (SUPPORTED_LANGUAGES.containsKey(languageCode)) {
            val prefs = context.getSharedPreferences(LANGUAGE_PREF_FILE, Context.MODE_PRIVATE)
            prefs.edit().putString(LANGUAGE_PREF, languageCode).apply()
        }
    }

    /**
     * Get all supported languages
     */
    fun getSupportedLanguages(): Map<String, String> {
        return SUPPORTED_LANGUAGES
    }

    /**
     * Get language name by code
     */
    fun getLanguageName(languageCode: String): String {
        return SUPPORTED_LANGUAGES[languageCode] ?: "Unknown"
    }

    /**
     * Translate text (placeholder - would integrate with translation API)
     */
    fun translate(text: String, fromLanguage: String, toLanguage: String): String {
        // This would call a translation API like Google Translate or similar
        // For now, returning the original text
        return text
    }

    /**
     * Get localized string resource
     */
    fun getLocalizedString(context: Context, key: String, language: String? = null): String {
        val lang = language ?: getCurrentLanguage(context)
        
        // This would fetch from a localization service or resource bundle
        // For now, returning a placeholder
        return getStringForLanguage(key, lang)
    }

    /**
     * Get string for specific language
     */
    private fun getStringForLanguage(key: String, language: String): String {
        // Localization strings map - would be loaded from a service
        val strings = mapOf(
            "en" to mapOf(
                "welcome" to "Welcome to Marketplace",
                "login" to "Login",
                "signup" to "Sign Up",
                "profile" to "Profile",
                "products" to "Products",
                "messages" to "Messages",
                "settings" to "Settings"
            ),
            "es" to mapOf(
                "welcome" to "Bienvenido al Mercado",
                "login" to "Iniciar sesión",
                "signup" to "Registrarse",
                "profile" to "Perfil",
                "products" to "Productos",
                "messages" to "Mensajes",
                "settings" to "Configuración"
            ),
            "fr" to mapOf(
                "welcome" to "Bienvenue sur la Marketplace",
                "login" to "Connexion",
                "signup" to "S'inscrire",
                "profile" to "Profil",
                "products" to "Produits",
                "messages" to "Messages",
                "settings" to "Paramètres"
            )
            // Add more languages as needed
        )
        
        return strings[language]?.get(key) ?: strings["en"]?.get(key) ?: key
    }

    /**
     * Format date according to language/locale
     */
    fun formatDate(timestamp: Long, language: String): String {
        val locale = Locale(language)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        
        return java.text.SimpleDateFormat("dd MMM yyyy", locale).format(calendar.time)
    }

    /**
     * Format currency according to language/locale
     */
    fun formatCurrency(amount: Double, language: String): String {
        val locale = Locale(language)
        val formatter = java.text.NumberFormat.getCurrencyInstance(locale)
        return formatter.format(amount)
    }

    /**
     * Format number according to language/locale
     */
    fun formatNumber(number: Number, language: String): String {
        val locale = Locale(language)
        val formatter = java.text.NumberFormat.getInstance(locale)
        return formatter.format(number)
    }

    /**
     * Get text direction for language (LTR or RTL)
     */
    fun getTextDirection(language: String): TextDirection {
        return when (language) {
            "ar", "he", "fa", "ur" -> TextDirection.RTL
            else -> TextDirection.LTR
        }
    }

    enum class TextDirection {
        LTR, RTL
    }

    /**
     * Check if language is supported
     */
    fun isLanguageSupported(languageCode: String): Boolean {
        return SUPPORTED_LANGUAGES.containsKey(languageCode)
    }

    /**
     * Get closest supported language for unsupported locale
     */
    fun getClosestSupportedLanguage(languageCode: String): String {
        return if (SUPPORTED_LANGUAGES.containsKey(languageCode)) {
            languageCode
        } else {
            // Try to find a language with the same base
            val baseLang = languageCode.split("-")[0]
            if (SUPPORTED_LANGUAGES.containsKey(baseLang)) {
                baseLang
            } else {
                "en" // Default to English
            }
        }
    }
}
