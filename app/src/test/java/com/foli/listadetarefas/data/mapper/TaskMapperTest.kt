package com.foli.listadetarefas.data.mapper

import com.foli.listadetarefas.data.local.db.TaskEntity
import com.foli.listadetarefas.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class TaskMapperTest {

    @Test
    fun toEntity_convertsDueDateToEpochDayAndKeepsNullableColumns() {
        val task = Task(
            id = "id-1",
            title = "Título",
            description = "Descrição",
            labelId = null,
            dueDate = LocalDate.of(2026, 11, 12),
            createdAt = 1_700_000_000_000L,
            completedAt = null,
        )

        val entity = TaskMapper.toEntity(task)

        assertEquals("id-1", entity.id)
        assertEquals(LocalDate.of(2026, 11, 12).toEpochDay(), entity.dueDate)
        assertEquals(1_700_000_000_000L, entity.createdAt)
        assertNull(entity.completedAt)
        assertNull(entity.labelId)
    }

    @Test
    fun toDomain_restoresLocalDateFromEpochDay() {
        val entity = TaskEntity(
            id = "id-1",
            title = "Título",
            description = "",
            dueDate = LocalDate.of(2026, 11, 12).toEpochDay(),
            createdAt = 42L,
        )

        val task = TaskMapper.toDomain(entity)

        assertEquals(LocalDate.of(2026, 11, 12), task.dueDate)
        assertEquals(42L, task.createdAt)
    }

    @Test
    fun roundTrip_preservesAllFields() {
        val task = Task(
            id = "id-2",
            title = "Comprar leite",
            description = "No mercado",
            createdAt = 99L,
            completedAt = 120L,
        )

        assertEquals(task, TaskMapper.toDomain(TaskMapper.toEntity(task)))
    }
}
