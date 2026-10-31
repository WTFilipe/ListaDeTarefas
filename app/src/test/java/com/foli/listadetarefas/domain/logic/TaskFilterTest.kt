package com.foli.listadetarefas.domain.logic

import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.model.TaskTab
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskFilterTest {

    private fun task(
        id: String,
        completedAt: Long? = null,
        title: String = "Tarefa $id",
        description: String = "",
        createdAt: Long = 0L,
    ) = Task(
        id = id,
        title = title,
        description = description,
        createdAt = createdAt,
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

    @Test
    fun completed_keepsOnlyCompletedTasks() {
        val active = task("a")
        val completed = task("b", completedAt = 10L)

        val result = TaskFilter.completed(listOf(active, completed))

        assertEquals(listOf("b"), result.map { it.id })
    }

    @Test
    fun byTab_activeAndCompletedAreComplementary() {
        val tasks = listOf(task("a"), task("b", completedAt = 1L), task("c"))

        assertEquals(listOf("a", "c"), TaskFilter.byTab(tasks, TaskTab.ACTIVE).map { it.id })
        assertEquals(listOf("b"), TaskFilter.byTab(tasks, TaskTab.COMPLETED).map { it.id })
    }

    @Test
    fun filterByQuery_matchesSubstringInTitle() {
        val tasks = listOf(
            task("a", title = "Comprar leite"),
            task("b", title = "Revisar proposta"),
        )

        val result = TaskFilter.filterByQuery(tasks, "leit")

        assertEquals(listOf("a"), result.map { it.id })
    }

    @Test
    fun filterByQuery_matchesSubstringInDescription() {
        val tasks = listOf(
            task("a", title = "Mercado", description = "Comprar leite e pão"),
            task("b", title = "Estudos", description = "Ler capítulo 3"),
        )

        val result = TaskFilter.filterByQuery(tasks, "leite")

        assertEquals(listOf("a"), result.map { it.id })
    }

    @Test
    fun filterByQuery_matchesTitleOrDescriptionOfSameTaskWithoutDuplicates() {
        val tasks = listOf(
            task("a", title = "Leite", description = "Comprar leite"),
            task("b", title = "Pão", description = "Comprar pão"),
        )

        val result = TaskFilter.filterByQuery(tasks, "leite")

        assertEquals(listOf("a"), result.map { it.id })
    }

    @Test
    fun filterByQuery_ignoresCase() {
        val tasks = listOf(task("a", title = "Comprar LEITE"))

        assertEquals(
            listOf("a"),
            TaskFilter.filterByQuery(tasks, "comprar leite").map { it.id },
        )
    }

    @Test
    fun filterByQuery_ignoresDiacriticsInBothDirections() {
        val tasks = listOf(
            task("a", title = "Árvore de decisão"),
            task("b", description = "Revisar a árvore genealógica"),
        )

        assertEquals(
            listOf("a", "b"),
            TaskFilter.filterByQuery(tasks, "arvore").map { it.id },
        )
    }

    @Test
    fun filterByQuery_trimsSurroundingSpacesFromQuery() {
        val tasks = listOf(task("a", title = "Comprar leite"))

        assertEquals(
            listOf("a"),
            TaskFilter.filterByQuery(tasks, "   leite   ").map { it.id },
        )
    }

    @Test
    fun filterByQuery_blankQueryReturnsAllTasksInOriginalOrder() {
        val tasks = listOf(task("new", createdAt = 300L), task("old", createdAt = 100L))

        assertEquals(tasks, TaskFilter.filterByQuery(tasks, ""))
        assertEquals(tasks, TaskFilter.filterByQuery(tasks, "    "))
    }

    @Test
    fun filterByQuery_noMatchReturnsEmptyList() {
        val tasks = listOf(task("a", title = "Comprar leite"))

        assertEquals(emptyList<Task>(), TaskFilter.filterByQuery(tasks, "academia"))
    }

    @Test
    fun filterByQuery_treatsSpecialCharactersLiterally() {
        val tasks = listOf(
            task("a", title = "Pagar 100% do cartão"),
            task("b", title = "Pagar 1000 do cartão"),
            task("c", title = "Ler [rascunho]"),
        )

        assertEquals(listOf("a"), TaskFilter.filterByQuery(tasks, "100%").map { it.id })
        assertEquals(listOf("c"), TaskFilter.filterByQuery(tasks, "[rascunho]").map { it.id })
        assertEquals(emptyList<Task>(), TaskFilter.filterByQuery(tasks, ".*").map { it.id })
    }

    @Test
    fun filterByQuery_isStableAndKeepsInputOrder() {
        val tasks = listOf(
            task("new", title = "Comprar leite", createdAt = 300L),
            task("middle", title = "Comprar pão", createdAt = 200L),
            task("old", title = "Comprar leite", createdAt = 100L),
        )

        val result = TaskFilter.filterByQuery(tasks, "leite")

        assertEquals(listOf("new", "old"), result.map { it.id })
    }

    @Test
    fun filterByQuery_scopedByTab_neverLeavesTheVisibleTab() {
        val tasks = listOf(
            task("active-leite", title = "Comprar leite"),
            task("completed-leite", title = "Comprar leite", completedAt = 10L),
            task("active-pao", title = "Comprar pão"),
        )

        val active = TaskFilter.byTab(tasks, TaskTab.ACTIVE)
        val completed = TaskFilter.byTab(tasks, TaskTab.COMPLETED)

        assertEquals(
            listOf("active-leite"),
            TaskFilter.filterByQuery(active, "leite").map { it.id },
        )
        assertEquals(
            listOf("completed-leite"),
            TaskFilter.filterByQuery(completed, "leite").map { it.id },
        )
    }
}
