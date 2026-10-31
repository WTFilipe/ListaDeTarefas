package com.foli.listadetarefas.domain.usecase.task

import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.domain.model.TaskDraft
import com.foli.listadetarefas.testing.FakeTaskRepository
import com.foli.listadetarefas.testing.FakeTimeProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateTaskUseCaseTest {

    private val repository = FakeTaskRepository()
    private val timeProvider = FakeTimeProvider(nowMillisValue = 1_700_000_000_000L)
    private val useCase = CreateTaskUseCase(repository = repository, timeProvider = timeProvider)

    @Test
    fun invoke_rejectsBlankTitleWithoutWriting() = runTest {
        val result = useCase(TaskDraft(id = "id-1", title = "   \n\t ", description = "descrição"))

        assertTrue(result is AppResult.Failure)
        assertEquals(AppFailure.EmptyTitle, (result as AppResult.Failure).failure)
        assertEquals(0, repository.upsertCalls)
        assertTrue(repository.tasks.value.isEmpty())
    }

    @Test
    fun invoke_trimsTitleAndDescriptionAndPersistsWithProvidedId() = runTest {
        val result = useCase(
            TaskDraft(id = "id-1", title = "  Comprar leite  ", description = "  no mercado  "),
        )

        assertTrue(result is AppResult.Success)
        val saved = repository.tasks.value.single()
        assertEquals("id-1", saved.id)
        assertEquals("Comprar leite", saved.title)
        assertEquals("no mercado", saved.description)
        assertEquals(timeProvider.nowMillisValue, saved.createdAt)
    }

    @Test
    fun invoke_descriptionWithOnlySpaces_isStoredAsEmpty() = runTest {
        useCase(TaskDraft(id = "id-1", title = "Título", description = "      "))

        assertEquals("", repository.tasks.value.single().description)
    }

    @Test
    fun invoke_persistsOptionalDueDate() = runTest {
        val dueDate = java.time.LocalDate.of(2026, 11, 12)

        useCase(TaskDraft(id = "id-1", title = "Título", description = "", dueDate = dueDate))

        assertEquals(dueDate, repository.tasks.value.single().dueDate)
    }

    @Test
    fun update_persistsDueDateWithoutChangingCreatedAt() = runTest {
        val original = com.foli.listadetarefas.domain.model.Task(
            id = "id-1",
            title = "Antiga",
            description = "",
            dueDate = java.time.LocalDate.of(2026, 11, 12),
            createdAt = 42L,
        )
        repository.tasks.value = listOf(original)
        val update = UpdateTaskUseCase(repository)

        update(
            TaskDraft(
                id = "id-1",
                title = "Nova",
                description = "",
                dueDate = java.time.LocalDate.of(2026, 12, 1),
            ),
        )

        val saved = repository.tasks.value.single()
        assertEquals(java.time.LocalDate.of(2026, 12, 1), saved.dueDate)
        assertEquals(42L, saved.createdAt)
    }

    @Test
    fun invoke_repeatedDraft_doesNotDuplicateTheTask() = runTest {
        val draft = TaskDraft(id = "id-fixo", title = "Título", description = "")

        useCase(draft)
        useCase(draft)

        assertEquals(1, repository.tasks.value.size)
        assertEquals("id-fixo", repository.tasks.value.single().id)
    }

    @Test
    fun invoke_allowsTwoDifferentTasksWithTheSameTitle() = runTest {
        useCase(TaskDraft(id = "id-1", title = "Título igual", description = ""))
        useCase(TaskDraft(id = "id-2", title = "Título igual", description = ""))

        assertEquals(2, repository.tasks.value.size)
    }

    @Test
    fun invoke_mapsStorageFailureWithoutLeakingExceptions() = runTest {
        repository.failUpsert = true

        val result = useCase(TaskDraft(id = "id-1", title = "Título", description = ""))

        assertTrue(result is AppResult.Failure)
        assertEquals(AppFailure.StorageUnavailable, (result as AppResult.Failure).failure)
    }
}
