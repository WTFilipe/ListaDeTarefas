package com.foli.listadetarefas.domain.logic

import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.domain.model.TaskLimits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskValidatorTest {

    @Test
    fun validate_trimsTitleAndDescription() {
        val result = TaskValidator.validate("  Comprar leite  ", "  no mercado  ")

        assertTrue(result is AppResult.Success)
        val normalized = (result as AppResult.Success).data
        assertEquals("Comprar leite", normalized.title)
        assertEquals("no mercado", normalized.description)
    }

    @Test
    fun validate_emptyTitle_isRejected() {
        val result = TaskValidator.validate("", "descrição")

        assertTrue(result is AppResult.Failure)
        assertEquals(AppFailure.EmptyTitle, (result as AppResult.Failure).failure)
    }

    @Test
    fun validate_blankTitleWithSpacesAndNewlines_isRejected() {
        assertTrue(TaskValidator.validate("   ", "x") is AppResult.Failure)
        assertTrue(TaskValidator.validate("\n\t\n", "x") is AppResult.Failure)
    }

    @Test
    fun validate_descriptionWithOnlySpaces_becomesEmpty() {
        val result = TaskValidator.validate("Título", "    ")

        assertTrue(result is AppResult.Success)
        assertEquals("", (result as AppResult.Success).data.description)
    }

    @Test
    fun validate_allowsDuplicateTitlesBetweenDistinctValidations() {
        val first = TaskValidator.validate("Título igual", "")
        val second = TaskValidator.validate("Título igual", "")

        assertTrue(first is AppResult.Success)
        assertTrue(second is AppResult.Success)
    }

    @Test
    fun validate_titleAtLimit_isAccepted() {
        val title = "a".repeat(TaskLimits.TITLE_MAX)

        val result = TaskValidator.validate(title, "")

        assertTrue(result is AppResult.Success)
        assertEquals(title, (result as AppResult.Success).data.title)
    }

    @Test
    fun validate_titleAboveLimit_isRejectedAsTextTooLong() {
        val result = TaskValidator.validate("a".repeat(TaskLimits.TITLE_MAX + 1), "")

        assertTrue(result is AppResult.Failure)
        assertEquals(AppFailure.TextTooLong, (result as AppResult.Failure).failure)
    }

    @Test
    fun validate_descriptionAtLimit_isAccepted() {
        val description = "b".repeat(TaskLimits.DESCRIPTION_MAX)

        val result = TaskValidator.validate("Título", description)

        assertTrue(result is AppResult.Success)
        assertEquals(description, (result as AppResult.Success).data.description)
    }

    @Test
    fun validate_descriptionAboveLimit_isRejectedAsTextTooLong() {
        val result = TaskValidator.validate("Título", "b".repeat(TaskLimits.DESCRIPTION_MAX + 1))

        assertTrue(result is AppResult.Failure)
        assertEquals(AppFailure.TextTooLong, (result as AppResult.Failure).failure)
    }

    @Test
    fun validate_trimsBeforeCheckingLimits() {
        val title = "  " + "a".repeat(TaskLimits.TITLE_MAX) + "  "

        val result = TaskValidator.validate(title, "")

        assertTrue(result is AppResult.Success)
        assertEquals(TaskLimits.TITLE_MAX, (result as AppResult.Success).data.title.length)
    }

    @Test
    fun limitTitle_keepsAcceptedPrefixOnly() {
        val raw = "a".repeat(TaskLimits.TITLE_MAX) + "excedente"

        val limited = TaskValidator.limitTitle(raw)

        assertEquals(TaskLimits.TITLE_MAX, limited.length)
        assertEquals(raw.take(TaskLimits.TITLE_MAX), limited)
    }

    @Test
    fun limitDescription_keepsAcceptedPrefixOnly() {
        val raw = "b".repeat(TaskLimits.DESCRIPTION_MAX) + "excedente"

        val limited = TaskValidator.limitDescription(raw)

        assertEquals(TaskLimits.DESCRIPTION_MAX, limited.length)
        assertEquals(raw.take(TaskLimits.DESCRIPTION_MAX), limited)
    }

    @Test
    fun limit_doesNotChangeTextBelowTheLimit() {
        assertEquals("Título curto", TaskValidator.limitTitle("Título curto"))
        assertEquals("Descrição curta", TaskValidator.limitDescription("Descrição curta"))
    }
}
