package com.berling.marketplace.utils

import java.util.regex.Pattern

/**
 * InputSanitizer handles sanitization of user inputs to prevent XSS and injection attacks
 */
object InputSanitizer {

    private val HTML_PATTERN = Pattern.compile("<[^>]*>")
    private val SCRIPT_PATTERN = Pattern.compile("(?i)<script[^>]*>.*?</script>")
    private val SQL_INJECTION_PATTERN = Pattern.compile("(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute)")

    /**
     * Sanitize text input by removing HTML and script tags
     */
    fun sanitizeText(input: String): String {
        var sanitized = input
        
        // Remove script tags
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("")
        
        // Remove HTML tags
        sanitized = HTML_PATTERN.matcher(sanitized).replaceAll("")
        
        // Trim whitespace
        sanitized = sanitized.trim()
        
        return sanitized
    }

    /**
     * Validate and sanitize product title
     */
    fun sanitizeProductTitle(title: String): String {
        val sanitized = sanitizeText(title)
        
        // Limit length
        return if (sanitized.length > 100) {
            sanitized.substring(0, 100)
        } else {
            sanitized
        }
    }

    /**
     * Validate and sanitize product description
     */
    fun sanitizeProductDescription(description: String): String {
        val sanitized = sanitizeText(description)
        
        // Limit length
        return if (sanitized.length > 2000) {
            sanitized.substring(0, 2000)
        } else {
            sanitized
        }
    }

    /**
     * Validate and sanitize user bio
     */
    fun sanitizeUserBio(bio: String): String {
        val sanitized = sanitizeText(bio)
        
        // Limit length
        return if (sanitized.length > 500) {
            sanitized.substring(0, 500)
        } else {
            sanitized
        }
    }

    /**
     * Validate email format
     */
    fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$"
        )
        return emailPattern.matcher(email).matches()
    }

    /**
     * Check if input contains potential SQL injection
     */
    fun containsSQLInjection(input: String): Boolean {
        return SQL_INJECTION_PATTERN.matcher(input).find()
    }

    /**
     * Escape special characters for database storage
     */
    fun escapeForDatabase(input: String): String {
        return input
            .replace("'", "\\'")
            .replace("\"", "\\\"")
            .replace("\\", "\\\\")
    }

    /**
     * Validate file name
     */
    fun isValidFileName(fileName: String): Boolean {
        val invalidChars = Pattern.compile("[<>:\"/\\\\|?*]")
        return !invalidChars.matcher(fileName).find() && fileName.isNotBlank()
    }

    /**
     * Get safe file extension
     */
    fun getSafeFileExtension(fileName: String): String? {
        val allowedExtensions = setOf("jpg", "jpeg", "png", "gif", "webp", "pdf", "doc", "docx")
        val lastDot = fileName.lastIndexOf(".")
        
        if (lastDot <= 0 || lastDot == fileName.length - 1) {
            return null
        }
        
        val extension = fileName.substring(lastDot + 1).lowercase()
        return if (extension in allowedExtensions) extension else null
    }

    /**
     * Remove potentially dangerous characters from product category
     */
    fun sanitizeCategory(category: String): String {
        return sanitizeText(category).take(50)
    }

    /**
     * Validate and sanitize location
     */
    fun sanitizeLocation(location: String): String {
        return sanitizeText(location).take(255)
    }

    /**
     * Validate price input
     */
    fun isValidPrice(price: String): Boolean {
        return try {
            val priceDouble = price.toDouble()
            priceDouble > 0 && priceDouble <= 1000000.0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Sanitize price to valid format
     */
    fun sanitizePrice(price: Double): Double {
        return when {
            price < 0 -> 0.0
            price > 1000000.0 -> 1000000.0
            else -> price
        }
    }

    /**
     * Check for profanity (basic implementation)
     */
    fun containsProfanity(input: String): Boolean {
        val profanityList = setOf(
            "badword1", "badword2" // Add actual profanity list
        )
        
        val lowerInput = input.lowercase()
        return profanityList.any { lowerInput.contains(it) }
    }

    /**
     * Remove excessive whitespace
     */
    fun normalizeWhitespace(input: String): String {
        return input.replace(Regex("\\s+"), " ").trim()
    }

    /**
     * Validate phone number format (basic)
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.replace(Regex("[^0-9+\\-()]"), "").length >= 10
    }

    /**
     * Validate date format (YYYY-MM-DD)
     */
    fun isValidDateFormat(date: String): Boolean {
        return date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
    }
}
