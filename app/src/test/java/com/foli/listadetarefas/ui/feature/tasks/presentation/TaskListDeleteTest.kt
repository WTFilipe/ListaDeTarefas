package com.foli.listadetarefas.ui.feature.tasks.presentation

import app.cash.turbine.test
import com.foli.listadetarefas.R
import com.foli.listadetarefas.core.analytics.NoOpAnalyticsTracker
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.usecase.label.ObserveLabelsUseCase
import com.foli.listadetarefas.domain.usecase.preferences.ObserveUserPreferencesUseCase
import com.foli.listadetarefas.domain.usecase.preferences.SetSelectedTabUseCase
import com.foli.listadetarefas.domain.usecase.task.DeleteTaskUseCase
import com.foli.listadetarefas.domain.usecase.task.ObserveTasksUseCase
import com.foli.listadetarefas.domain.usecase.task.SetTaskCompletedUseCase
import com.foli.listadetarefas.testing.FakeLabelRepository
import com.foli.listadetarefas.testing.FakePreferencesRepository
import com.foli.listadetarefas.testing.FakeTaskRepository
import com.foli.listadetarefas.testing.FakeTimeProvider
import com.foli.listadetarefas.ui.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListDeleteTest {

    private fun task(id: String) = Task(
        id = id,
        title = "Tarefa $id",
        description = "",
        createdAt = 100L,
    )

    private fun createViewModel(
        repository: FakeTaskRepository,
        preferences: FakePreferencesRepository = FakePreferencesRepository(),
    ) = TaskListViewModel(
        observeTasksUseCase = ObserveTasksUseCase(repository),
        observeLabelsUseCase = ObserveLabelsUseCase(FakeLabelRepository()),
        deleteTaskUseCase = DeleteTaskUseCase(repository),
        setTaskCompletedUseCase = SetTaskCompletedUseCase(repository, FakeTimeProvider()),
        observeUserPreferencesUseCase = ObserveUserPreferencesUseCase(preferences),
        setSelectedTabUseCase = SetSelectedTabUseCase(preferences),
        analyticsTracker = NoOpAnalyticsTracker(),
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun deleteRequested_opensPendingDeleteWithoutDeleting() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(task("id-1"))
        }
        val viewModel = createViewModel(repository)

        viewModel.onAction(TaskListAction.DeleteRequested("id-1"))

        assertEquals("id-1", viewModel.state.value.pendingDeleteId)
        assertEquals(0, repository.deleteCalls)
        assertEquals(1, repository.tasks.value.size)
    }

    @Test
    fun deleteDismissed_keepsTaskIntact() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(task("id-1"))
        }
        val viewModel = createViewModel(repository)

        viewModel.onAction(TaskListAction.DeleteRequested("id-1"))
        viewModel.onAction(TaskListAction.DeleteDismissed)
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.state.value.pendingDeleteId)
        assertEquals(0, repository.deleteCalls)
        assertEquals(1, repository.tasks.value.size)
    }

    @Test
    fun deleteConfirmed_removesTaskAndClearsPending() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(task("id-1"), task("id-2"))
        }
        val viewModel = createViewModel(repository)

        viewModel.onAction(TaskListAction.DeleteRequested("id-1"))
        viewModel.onAction(TaskListAction.DeleteConfirmed)
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.state.value.pendingDeleteId)
        assertEquals(1, repository.deleteCalls)
        assertEquals(listOf("id-2"), repository.tasks.value.map { it.id })
    }

    @Test
    fun deleteFailure_emitsRetryOperationAndRetryRepeatsSameDelete() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(task("id-1"))
            failDelete = true
        }
        val viewModel = createViewModel(repository)

        viewModel.effects.test {
            viewModel.onAction(TaskListAction.DeleteRequested("id-1"))
            viewModel.onAction(TaskListAction.DeleteConfirmed)
            val effect = awaitItem()

            assertTrue(effect is TaskListEffect.ShowStorageError)
            val storageError = effect as TaskListEffect.ShowStorageError
            assertEquals(UiText.Res(R.string.storage_delete_error_message), storageError.message)
            assertEquals(TaskRetryOperation.DeleteTask("id-1"), storageError.operation)
        }

        repository.failDelete = false
        viewModel.onAction(TaskListAction.RetryRequested(TaskRetryOperation.DeleteTask("id-1")))
        testScheduler.advanceUntilIdle()

        assertEquals(2, repository.deleteCalls)
        assertTrue(repository.tasks.value.isEmpty())
    }
}
