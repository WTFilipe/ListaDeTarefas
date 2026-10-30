package com.foli.listadetarefas.domain.logic

import com.foli.listadetarefas.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskFilterTest {

    private fun task(id: String, completedAt: Long? = null) = Task(
        id = id,
        title = "Tarefa $id",
        description = "",
        createdAt = 0L,
        completedAt = completedAt,
    )

    @Test
    fun active_keepsOnlyNotCompletedTasks() {
        val activeA = task("a")
        val completed = task("b", completedAt = 10L)
        val activeC = task("c")

        val result = TaskFilter.active(listOf(activeA, completed, activeC))

        assertEquals(listOf("a", "c"), result.map { it.id })
    }

    @Test
    fun active_returnsEmptyWhenAllCompleted() {
        val result = TaskFilter.active(listOf(task("a", completedAt = 1L)))

        assertEquals(emptyList<Task>(), result)
    }
}
