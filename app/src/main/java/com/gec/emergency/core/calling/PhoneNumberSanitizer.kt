package com.gec.emergency.core.calling

/**
 * Sanitizes and validates telephone number strings before passing them to system dialer intents.
 *
 * <p>Removes presentation formatting characters (spaces, dashes, parentheses) while preserving
 * leading international {@code +} prefixes and digits required for dialling.
 */
object PhoneNumberSanitizer {

    /**
     * Sanitizes a phone number input string.
     *
     * @param phoneNumber raw phone number string
     * @return cleaned phone number digits string, or null if invalid/blank
     */
    fun sanitize(phoneNumber: String?): String? {
        if (phoneNumber.isNullOrBlank()) return null

        val trimmed = phoneNumber.trim()
        val hasLeadingPlus = trimmed.startsWith("+")

        val digitsOnly = trimmed.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) return null

        return if (hasLeadingPlus) "+$digitsOnly" else digitsOnly
    }

    /**
     * Checks if a phone number string can be sanitized into a valid dialable number.
     */
    fun isValid(phoneNumber: String?): Boolean {
        return sanitize(phoneNumber) != null
    }
}
