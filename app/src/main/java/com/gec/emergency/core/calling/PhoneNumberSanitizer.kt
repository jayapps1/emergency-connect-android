package com.gec.emergency.core.calling

object PhoneNumberSanitizer {

    fun sanitize(phoneNumber: String?): String? {
        if (phoneNumber.isNullOrBlank()) return null

        val trimmed = phoneNumber.trim()
        val hasLeadingPlus = trimmed.startsWith("+")

        val digitsOnly = trimmed.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) return null

        return if (hasLeadingPlus) "+$digitsOnly" else digitsOnly
    }

    fun isValid(phoneNumber: String?): Boolean {
        return sanitize(phoneNumber) != null
    }
}
