package com.foli.listadetarefas.domain.usecase.task

import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.testing.FakeTaskRepository
import com.foli.listadetarefas.testing.FakeTimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class SetTaskCompletedUseCaseTest {

    private fun task(id: String) = Task(
        id = id,
        title = "Tarefa $id",
        description = "Descrição",
        createdAt = 100L,
    )

    @Test
    fun complete_recordsCompletionUsingTimeProviderAndKeepsCreatedAt() = runTest {
        val repository = FakeTaskRepository().apply { tasks.value = listOf(task("id-1")) }
        val useCase = SetTaskCompletedUseCase(
            repository = repository,
            timeProvider = FakeTimeProvider(nowMillisValue = 5_000L),
        )

        val result = useCase("id-1", isCompleted = true)

        assertTrue(result is AppResult.Success)
        val stored = repository.tasks.value.single()
        assertEquals(5_000L, stored.completedAt)
        assertEquals(100L, stored.createdAt)
        assertEquals("Descrição", stored.description)
        assertEquals(1, repository.setCompletedCalls)
    }

    @Test
    fun reopen_clearsCompletionAndPreservesAllOtherData() = runTest {
        val completed = task("id-1").copy(completedAt = 999L)
        val repository = FakeTaskRepository().apply { tasks.value = listOf(completed) }
        val useCase = SetTaskCompletedUseCase(
            repository = repository,
            timeProvider = FakeTimeProvider(todayValue = LocalDate.of(2026, 1, 1)),
        )

        useCase("id-1", isCompleted = false)

        assertNull(repository.tasks.value.single().completedAt)
        assertEquals(100L, repository.tasks.value.single().createdAt)
    }

    @Test
    fun storageFailure_isPropagatedWithoutThrowing() = runTest {
        val repository = FakeTaskRepository().apply { failSetCompleted = true }
        val useCase = SetTaskCompletedUseCase(repository, FakeTimeProvider())

        val result = useCase("id-1", isCompleted = true)

        assertEquals(
            AppFailure.StorageUnavailable,
            (result as AppResult.Failure).failure,
        )
    }
}
