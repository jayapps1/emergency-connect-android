package com.gec.emergency.core.calling

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberSanitizerTest {

    @Test
    fun sanitize_nullOrEmptyString_returnsNull() {
        assertNull(PhoneNumberSanitizer.sanitize(null))
        assertNull(PhoneNumberSanitizer.sanitize(""))
    }

    @Test
    fun sanitize_whitespaceString_returnsNull() {
        assertNull(PhoneNumberSanitizer.sanitize("   "))
        assertNull(PhoneNumberSanitizer.sanitize("\t\n"))
    }

    @Test
    fun sanitize_normalNumber_returnsSameNumber() {
        assertEquals("192", PhoneNumberSanitizer.sanitize("192"))
        assertEquals("0302000000", PhoneNumberSanitizer.sanitize("0302000000"))
    }

    @Test
    fun sanitize_plusCodeFormat_preservesPlusPrefix() {
        assertEquals("+233302000000", PhoneNumberSanitizer.sanitize("+233302000000"))
    }

    @Test
    fun sanitize_numberWithSpacesAndDashes_removesFormatting() {
        assertEquals("+233302000000", PhoneNumberSanitizer.sanitize("+233 302-000-000"))
        assertEquals("0302000000", PhoneNumberSanitizer.sanitize("0302 000 - 000"))
    }

    @Test
    fun sanitize_numberWithParentheses_removesParentheses() {
        assertEquals("+233302000000", PhoneNumberSanitizer.sanitize("+233 (302) 000000"))
    }

    @Test
    fun isValid_validAndInvalidNumbers_returnsExpectedBoolean() {
        assertTrue(PhoneNumberSanitizer.isValid("192"))
        assertTrue(PhoneNumberSanitizer.isValid("+233 302 000 000"))
        assertFalse(PhoneNumberSanitizer.isValid(""))
        assertFalse(PhoneNumberSanitizer.isValid("   "))
        assertFalse(PhoneNumberSanitizer.isValid("abc---"))
    }
}
