package com.foli.listadetarefas.domain.logic

import com.foli.listadetarefas.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskSorterTest {

    private fun task(id: String, createdAt: Long) = Task(
        id = id,
        title = "Tarefa $id",
        description = "",
        createdAt = createdAt,
    )

    @Test
    fun sort_putsMostRecentCreationFirst() {
        val oldest = task("a", 100L)
        val newest = task("b", 300L)
        val middle = task("c", 200L)

        val sorted = TaskSorter.sort(listOf(oldest, newest, middle))

        assertEquals(listOf("b", "c", "a"), sorted.map { it.id })
    }

    @Test
    fun sort_breaksTiesDeterministicallyById() {
        val first = task("z", 100L)
        val second = task("a", 100L)
        val third = task("m", 100L)

        val sorted = TaskSorter.sort(listOf(first, second, third))

        assertEquals(listOf("a", "m", "z"), sorted.map { it.id })
    }
}
