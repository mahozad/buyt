package com.pleon.buyt.util

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.*

class FormatterUtilTest {

    private val defaultLocale = Locale.US // Or getDefault()

    @Test
    fun `format a latin number`() {
        val number = 39701
        val result = formatNumber(number)
        assertThat(result).isEqualTo("39,701")
    }

    @Test
    fun `format a valid number string`() {
        val numberString = "39701"
        val result = formatPrice(numberString)
        assertThat(result).isEqualTo("39,701")
    }

    @Test
    fun `formatting an invalid number string should throw NumberFormatException`() {
        val numberString = "$39701"
        assertThatExceptionOfType(NumberFormatException::class.java)
                .isThrownBy { formatPrice(numberString) }
                .withMessageContaining(numberString)
    }

    @Test
    fun `format a date with EN locale`() {
        Locale.setDefault(Locale.US)
        val date = SimpleDateFormat("yyyy-MM-dd").parse("2015-04-03")
        val result = formatDate(date)
        assertThat(result).isEqualTo("04/03/2015")
    }

    @Test
    fun `format a date with FA locale`() {
        val date = SimpleDateFormat("yyyy-MM-dd").parse("2015-04-03")
        Locale.setDefault(Locale.forLanguageTag("fa"))
        val result = formatDate(date)
        assertThat(result).isEqualTo("جمعه ۱۴ فروردین ۱۳۹۴")
    }

    @AfterEach
    fun tearDown() {
        // Reset the locale for subsequent test cases
        Locale.setDefault(defaultLocale)
    }
}
