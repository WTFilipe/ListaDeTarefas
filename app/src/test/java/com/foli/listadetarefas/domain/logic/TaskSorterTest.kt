package com.foli.listadetarefas.domain.logic

import com.foli.listadetarefas.domain.model.SortOrder
import com.foli.listadetarefas.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TaskSorterTest {

    private fun task(
        id: String,
        createdAt: Long,
        title: String = "Tarefa $id",
        dueDate: LocalDate? = null,
        completedAt: Long? = null,
    ) = Task(
        id = id,
        title = title,
        description = "",
        dueDate = dueDate,
        createdAt = createdAt,
        completedAt = completedAt,
    )

    private fun ids(tasks: List<Task>) = tasks.map { it.id }

    @Test
    fun sort_defaultsToMostRecentCreationFirst() {
        val oldest = task("a", 100L)
        val newest = task("b", 300L)
        val middle = task("c", 200L)

        val sorted = TaskSorter.sort(listOf(oldest, newest, middle))

        assertEquals(listOf("b", "c", "a"), ids(sorted))
    }

    @Test
    fun sort_breaksCreationTiesDeterministicallyById() {
        val first = task("z", 100L)
        val second = task("a", 100L)
        val third = task("m", 100L)

        val sorted = TaskSorter.sort(listOf(first, second, third), SortOrder.CREATED_DESC)

        assertEquals(listOf("a", "m", "z"), ids(sorted))
    }

    @Test
    fun createdAscending_putsOldestFirst() {
        val sorted = TaskSorter.sort(
            listOf(task("new", 300L), task("old", 100L), task("middle", 200L)),
            SortOrder.CREATED_ASC,
        )

        assertEquals(listOf("old", "middle", "new"), ids(sorted))
    }

    @Test
    fun creationOrder_ignoresCompletionStateAndTitle() {
        val completedNewest = task("done", 300L, title = "zzz", completedAt = 999L)
        val activeOldest = task("active", 100L, title = "aaa")

        val descending = TaskSorter.sort(
            listOf(activeOldest, completedNewest),
            SortOrder.CREATED_DESC,
        )
        val ascending = TaskSorter.sort(
            listOf(completedNewest, activeOldest),
            SortOrder.CREATED_ASC,
        )

        assertEquals(listOf("done", "active"), ids(descending))
        assertEquals(listOf("active", "done"), ids(ascending))
    }

    @Test
    fun titleAscending_ignoresCaseAndAccents() {
        val sorted = TaskSorter.sort(
            listOf(
                task("z", 100L, title = "Zebra"),
                task("a", 200L, title = "água"),
                task("m", 300L, title = "Mercado"),
                task("o", 400L, title = "Órbita"),
            ),
            SortOrder.TITLE_ASC,
        )

        assertEquals(listOf("a", "m", "o", "z"), ids(sorted))
    }

    @Test
    fun titleDescending_ignoresCaseAndAccents() {
        val sorted = TaskSorter.sort(
            listOf(
                task("z", 100L, title = "Zebra"),
                task("a", 200L, title = "água"),
                task("m", 300L, title = "Mercado"),
                task("o", 400L, title = "Órbita"),
            ),
            SortOrder.TITLE_DESC,
        )

        assertEquals(listOf("z", "o", "m", "a"), ids(sorted))
    }

    @Test
    fun titleTies_breakByMostRecentCreation() {
        val older = task("older", 100L, title = "Árvore")
        val newer = task("newer", 300L, title = "arvore")

        val ascending = TaskSorter.sort(listOf(older, newer), SortOrder.TITLE_ASC)
        val descending = TaskSorter.sort(listOf(newer, older), SortOrder.TITLE_DESC)

        assertEquals(listOf("newer", "older"), ids(ascending))
        assertEquals(listOf("newer", "older"), ids(descending))
    }

    @Test
    fun titleTies_withSameCreation_breakById() {
        val sorted = TaskSorter.sort(
            listOf(
                task("z", 100L, title = "Igual"),
                task("a", 100L, title = "Igual"),
            ),
            SortOrder.TITLE_ASC,
        )

        assertEquals(listOf("a", "z"), ids(sorted))
    }

    @Test
    fun titleOrdering_usesPtBrCollatorInsteadOfRawStringOrder() {
        // Em `String.compareTo` cru, "Órbita" (U+00D3 = 211) viria DEPOIS de
        // "Zebra" (Z = 90). O `Collator` pt-BR com força PRIMARY (ADR-0008)
        // coloca "Órbita" entre "Mercado" e "Zebra".
        val sorted = TaskSorter.sort(
            listOf(
                task("z", 100L, title = "Zebra"),
                task("o", 200L, title = "Órbita"),
                task("m", 300L, title = "Mercado"),
            ),
            SortOrder.TITLE_ASC,
        )

        assertEquals(listOf("m", "o", "z"), ids(sorted))
    }

    @Test
    fun titleOrdering_treatsCaseAndAccentVariantsAsTheSameWord() {
        // PRIMARY ignora caixa e acento: "Ação", "acao", "AÇÃO" e "Acao"
        // empatam no título e caem no desempate por criação mais recente.
        val sorted = TaskSorter.sort(
            listOf(
                task("1", 100L, title = "acao"),
                task("2", 400L, title = "AÇÃO"),
                task("3", 200L, title = "Ação"),
                task("4", 300L, title = "Acao"),
            ),
            SortOrder.TITLE_ASC,
        )

        assertEquals(listOf("2", "4", "3", "1"), ids(sorted))
    }

    @Test
    fun deadlineAscending_putsTasksWithoutDateLast() {
        val sorted = TaskSorter.sort(
            listOf(
                task("none", 500L, dueDate = null),
                task("late", 100L, dueDate = LocalDate.of(2026, 12, 1)),
                task("soon", 200L, dueDate = LocalDate.of(2026, 10, 1)),
            ),
            SortOrder.DEADLINE_ASC,
        )

        assertEquals(listOf("soon", "late", "none"), ids(sorted))
    }

    @Test
    fun deadlineDescending_putsTasksWithoutDateLast() {
        val sorted = TaskSorter.sort(
            listOf(
                task("none", 500L, dueDate = null),
                task("late", 100L, dueDate = LocalDate.of(2026, 12, 1)),
                task("soon", 200L, dueDate = LocalDate.of(2026, 10, 1)),
            ),
            SortOrder.DEADLINE_DESC,
        )

        assertEquals(listOf("late", "soon", "none"), ids(sorted))
    }

    @Test
    fun deadlineTies_breakByMostRecentCreation() {
        val dueDate = LocalDate.of(2026, 10, 30)
        val older = task("older", 100L, dueDate = dueDate)
        val newer = task("newer", 300L, dueDate = dueDate)

        val ascending = TaskSorter.sort(listOf(older, newer), SortOrder.DEADLINE_ASC)
        val descending = TaskSorter.sort(listOf(newer, older), SortOrder.DEADLINE_DESC)

        assertEquals(listOf("newer", "older"), ids(ascending))
        assertEquals(listOf("newer", "older"), ids(descending))
    }

    @Test
    fun deadlineWithAllTasksMissingDate_keepsCreationDescending() {
        val sorted = TaskSorter.sort(
            listOf(task("old", 100L), task("new", 300L), task("middle", 200L)),
            SortOrder.DEADLINE_ASC,
        )

        assertEquals(listOf("new", "middle", "old"), ids(sorted))
    }
}
