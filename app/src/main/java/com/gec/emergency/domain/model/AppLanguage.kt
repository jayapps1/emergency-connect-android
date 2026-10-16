package com.gec.emergency.domain.model

enum class AppLanguage(
    val code: String,
    val displayName: String,
) {
    ENGLISH(code = "en", displayName = "English"),
    TWI(code = "tw", displayName = "Twi"),
    GA(code = "gaa", displayName = "Ga"),
    EWE(code = "ee", displayName = "Ewe");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}
