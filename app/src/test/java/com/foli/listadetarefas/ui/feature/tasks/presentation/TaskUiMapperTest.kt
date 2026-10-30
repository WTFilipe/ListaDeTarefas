package com.foli.listadetarefas.ui.feature.tasks.presentation

import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.model.TaskLabel
import com.foli.listadetarefas.ui.model.DeadlineUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TaskUiMapperTest {

    private val today = LocalDate.of(2026, 10, 30)

    private fun task(
        completedAt: Long? = null,
        dueDate: LocalDate? = null,
    ) = Task(
        id = "id-1",
        title = "Comprar leite",
        description = "No mercado",
        dueDate = dueDate,
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
        val uiModel = TaskUiMapper.toUiModel(
            task = task(completedAt = 900L),
            formatCompletedOn = { "12/11/2026" },
        )

        assertTrue(uiModel.isCompleted)
        assertEquals("12/11/2026", uiModel.completedOnDate)
        assertEquals("Comprar leite", uiModel.title)
        assertEquals("No mercado", uiModel.description)
    }

    @Test
    fun deadline_isClassifiedByInjectedToday() {
        val overdue = TaskUiMapper.toUiModel(task(dueDate = today.minusDays(1)), today = today)
        val todayModel = TaskUiMapper.toUiModel(task(dueDate = today), today = today)
        val tomorrow = TaskUiMapper.toUiModel(task(dueDate = today.plusDays(1)), today = today)
        val soon = TaskUiMapper.toUiModel(task(dueDate = today.plusDays(7)), today = today)

        assertEquals(DeadlineUiState.OVERDUE, overdue.deadline)
        assertEquals("29/10/2026", overdue.deadlineDateLabel)
        assertEquals(DeadlineUiState.TODAY, todayModel.deadline)
        assertEquals(DeadlineUiState.TOMORROW, tomorrow.deadline)
        assertEquals(DeadlineUiState.SOON, soon.deadline)
    }

    @Test
    fun deadline_absentWhenNoDueDateTooFarOrCompleted() {
        val noDate = TaskUiMapper.toUiModel(task(), today = today)
        val tooFar = TaskUiMapper.toUiModel(task(dueDate = today.plusDays(8)), today = today)
        val completed = TaskUiMapper.toUiModel(
            task(completedAt = 900L, dueDate = today),
            today = today,
        )

        assertNull(noDate.deadline)
        assertNull(tooFar.deadline)
        assertNull(completed.deadline)
        assertNull(completed.deadlineDateLabel)
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
