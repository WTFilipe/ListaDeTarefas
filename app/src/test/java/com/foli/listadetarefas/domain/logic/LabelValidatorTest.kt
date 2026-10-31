package com.foli.listadetarefas.domain.logic

import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.domain.model.TaskLimits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LabelValidatorTest {

    @Test
    fun validName_isTrimmedAndKeepsDisplayText() {
        val result = LabelValidator.validate("  Trabalho  ")

        val data = (result as AppResult.Success).data
        assertEquals("Trabalho", data.name)
        assertEquals("trabalho", data.nameKey)
    }

    @Test
    fun blankName_isRejected() {
        val result = LabelValidator.validate("   ")

        assertEquals(AppFailure.EmptyLabelName, (result as AppResult.Failure).failure)
    }

    @Test
    fun nameAboveLimit_isRejected() {
        val result = LabelValidator.validate("a".repeat(TaskLimits.LABEL_NAME_MAX + 1))

        assertEquals(AppFailure.TextTooLong, (result as AppResult.Failure).failure)
    }

    @Test
    fun nameKey_ignoresCaseAndAccents() {
        assertEquals(
            LabelValidator.nameKey("Ação"),
            LabelValidator.nameKey("acao"),
        )
        assertTrue(LabelValidator.nameKey("AÇÃO") == LabelValidator.nameKey("Ação"))
    }

    @Test
    fun validateWithColor_keepsFixedPaletteKey() {
        val result = LabelValidator.validateWithColor("Estudos", LabelColorKey.BLUE)

        assertEquals(LabelColorKey.BLUE, (result as AppResult.Success).data.colorKey)
    }

    @Test
    fun limitName_truncatesToThirtyCharacters() {
        val long = "x".repeat(40)

        assertEquals(TaskLimits.LABEL_NAME_MAX, LabelValidator.limitName(long).length)
    }
}
