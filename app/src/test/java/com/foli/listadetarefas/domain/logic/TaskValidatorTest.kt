package com.foli.listadetarefas.domain.logic

import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
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
}
