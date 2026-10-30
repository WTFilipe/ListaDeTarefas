package com.foli.listadetarefas.ui.feature.tasks.presentation

import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.model.TaskLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskUiMapperTest {

    private fun task(completedAt: Long? = null) = Task(
        id = "id-1",
        title = "Comprar leite",
        description = "No mercado",
        createdAt = 100L,
        completedAt = completedAt,
    )

    @Test
    fun activeTask_isNotCompletedAndHasNoCompletionDate() {
        val uiModel = TaskUiMapper.toUiModel(task())

        assertFalse(uiModel.isCompleted)
        assertNull(uiModel.completedOnDate)
    }

    @Test
    fun completedTask_formatsCompletionDateWithInjectedFormatter() {
        val uiModel = TaskUiMapper.toUiModel(task(completedAt = 900L)) { "12/11/2026" }

        assertTrue(uiModel.isCompleted)
        assertEquals("12/11/2026", uiModel.completedOnDate)
        assertEquals("Comprar leite", uiModel.title)
        assertEquals("No mercado", uiModel.description)
    }

    @Test
    fun label_isMappedToUiModel_andNullStaysNull() {
        val label = TaskLabel("l1", "Trabalho", "trabalho", LabelColorKey.BLUE, 1L)

        val withLabel = TaskUiMapper.toUiModel(task(), label)
        val withoutLabel = TaskUiMapper.toUiModel(task())

        assertEquals("Trabalho", withLabel.label?.name)
        assertEquals(LabelColorKey.BLUE, withLabel.label?.colorKey)
        assertNull(withoutLabel.label)
    }
}
