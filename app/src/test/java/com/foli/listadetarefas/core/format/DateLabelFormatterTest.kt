package com.foli.listadetarefas.core.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class DateLabelFormatterTest {

    private val zone = ZoneOffset.UTC

    @Test
    fun format_returnsLocalDateWithoutTime() {
        val epochMillis = LocalDate.of(2026, 11, 12)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        assertEquals("12/11/2026", DateLabelFormatter.format(epochMillis, zone))
    }

    @Test
    fun format_padsDayAndMonthWithTwoDigits() {
        val epochMillis = LocalDate.of(2026, 1, 5)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        assertEquals("05/01/2026", DateLabelFormatter.format(epochMillis, zone))
    }

    @Test
    fun formatDate_formatsLocalDateAsDayMonthYear() {
        assertEquals(
            "07/03/2027",
            DateLabelFormatter.formatDate(LocalDate.of(2027, 3, 7)),
        )
    }
}
