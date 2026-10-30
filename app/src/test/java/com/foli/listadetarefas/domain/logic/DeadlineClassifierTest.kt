package com.foli.listadetarefas.domain.logic

import com.foli.listadetarefas.domain.model.DeadlineState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DeadlineClassifierTest {

    private val today = LocalDate.of(2026, 10, 30)

    @Test
    fun classify_returnsNull_whenDueDateIsNull() {
        assertNull(DeadlineClassifier.classify(today, null))
    }

    @Test
    fun classify_returnsOverdueForPastDates() {
        assertEquals(DeadlineState.OVERDUE, DeadlineClassifier.classify(today, today.minusDays(1)))
        assertEquals(DeadlineState.OVERDUE, DeadlineClassifier.classify(today, today.minusYears(3)))
    }

    @Test
    fun classify_returnsTodayTomorrowAndSoonAtBoundaries() {
        assertEquals(DeadlineState.TODAY, DeadlineClassifier.classify(today, today))
        assertEquals(DeadlineState.TOMORROW, DeadlineClassifier.classify(today, today.plusDays(1)))
        assertEquals(DeadlineState.SOON, DeadlineClassifier.classify(today, today.plusDays(2)))
        assertEquals(DeadlineState.SOON, DeadlineClassifier.classify(today, today.plusDays(7)))
    }

    @Test
    fun classify_returnsNull_afterSevenDays() {
        assertNull(DeadlineClassifier.classify(today, today.plusDays(8)))
        assertNull(DeadlineClassifier.classify(today, today.plusMonths(2)))
    }

    @Test
    fun classify_handlesMonthEndLeapYearAndYearBoundary() {
        assertEquals(
            DeadlineState.TOMORROW,
            DeadlineClassifier.classify(
                LocalDate.of(2024, 2, 28),
                LocalDate.of(2024, 2, 29),
            ),
        )
        assertEquals(
            DeadlineState.OVERDUE,
            DeadlineClassifier.classify(
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 2, 28),
            ),
        )
        assertEquals(
            DeadlineState.SOON,
            DeadlineClassifier.classify(
                LocalDate.of(2026, 12, 30),
                LocalDate.of(2027, 1, 3),
            ),
        )
    }
}
