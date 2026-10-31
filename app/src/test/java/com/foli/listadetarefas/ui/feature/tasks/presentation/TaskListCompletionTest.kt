package com.foli.listadetarefas.ui.feature.tasks.presentation

import androidx.lifecycle.SavedStateHandle

import app.cash.turbine.test
import com.foli.listadetarefas.R
import com.foli.listadetarefas.core.analytics.NoOpAnalyticsTracker
import com.foli.listadetarefas.core.time.TimeSignals
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.model.TaskTab
import com.foli.listadetarefas.domain.usecase.label.ObserveLabelsUseCase
import com.foli.listadetarefas.domain.usecase.preferences.ObserveUserPreferencesUseCase
import com.foli.listadetarefas.domain.usecase.preferences.SetSelectedTabUseCase
import com.foli.listadetarefas.domain.usecase.preferences.SetSortOrderUseCase
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
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListCompletionTest {

    private fun task(id: String, completedAt: Long? = null) = Task(
        id = id,
        title = "Tarefa $id",
        description = "Descrição $id",
        createdAt = 100L,
        completedAt = completedAt,
    )

    private fun createViewModel(
        repository: FakeTaskRepository,
        preferences: FakePreferencesRepository = FakePreferencesRepository(),
    ) = TaskListViewModel(
        observeTasksUseCase = ObserveTasksUseCase(repository),
        observeLabelsUseCase = ObserveLabelsUseCase(FakeLabelRepository()),
        deleteTaskUseCase = DeleteTaskUseCase(repository),
        setTaskCompletedUseCase = SetTaskCompletedUseCase(
            repository = repository,
            timeProvider = FakeTimeProvider(nowMillisValue = 5_000L),
        ),
        observeUserPreferencesUseCase = ObserveUserPreferencesUseCase(preferences),
        setSelectedTabUseCase = SetSelectedTabUseCase(preferences),
        setSortOrderUseCase = SetSortOrderUseCase(preferences),
        timeProvider = FakeTimeProvider(nowMillisValue = 5_000L),
        timeSignals = TimeSignals(),
        analyticsTracker = NoOpAnalyticsTracker(),
        savedStateHandle = SavedStateHandle(),
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun completion_movesTaskFromActiveToCompletedPreservingData() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply { tasks.value = listOf(task("id-1")) }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(TaskListAction.CompletionToggled("id-1", isCompleted = true))
        testScheduler.advanceUntilIdle()

        assertEquals(5_000L, repository.tasks.value.single().completedAt)
        assertEquals(100L, repository.tasks.value.single().createdAt)
        assertTrue(viewModel.state.value.rows.isEmpty())

        viewModel.onAction(TaskListAction.TabSelected(TaskTab.COMPLETED))
        testScheduler.advanceUntilIdle()

        val completed = viewModel.state.value.rows.single()
        assertEquals("id-1", completed.id)
        assertEquals("Tarefa id-1", completed.title)
        assertEquals("Descrição id-1", completed.description)
        assertTrue(completed.isCompleted)
        assertTrue(completed.completedOnDate != null)
    }

    @Test
    fun reopening_returnsTaskToActiveTab() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(task("id-1", completedAt = 900L))
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(TaskListAction.TabSelected(TaskTab.COMPLETED))
        viewModel.onAction(TaskListAction.CompletionToggled("id-1", isCompleted = false))
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.rows.isEmpty())
        assertEquals(null, repository.tasks.value.single().completedAt)

        viewModel.onAction(TaskListAction.TabSelected(TaskTab.ACTIVE))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("id-1"), viewModel.state.value.rows.map { it.id })
        assertFalse(viewModel.state.value.rows.single().isCompleted)
    }

    @Test
    fun selectedTab_isPersistedAndRestoredAfterRecreation() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository()
        val preferences = FakePreferencesRepository()
        val firstViewModel = createViewModel(repository, preferences)
        testScheduler.advanceUntilIdle()

        firstViewModel.onAction(TaskListAction.TabSelected(TaskTab.COMPLETED))
        testScheduler.advanceUntilIdle()

        assertEquals(1, preferences.setSelectedTabCalls)
        assertEquals(TaskTab.COMPLETED, preferences.preferences.value.selectedTab)

        val secondViewModel = createViewModel(repository, preferences)
        testScheduler.advanceUntilIdle()

        assertEquals(TaskTab.COMPLETED, secondViewModel.state.value.tab)
    }

    @Test
    fun completionFailure_emitsRetryOperationAndRetryRepeatsIt() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(task("id-1"))
            failSetCompleted = true
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onAction(TaskListAction.CompletionToggled("id-1", isCompleted = true))
            val effect = awaitItem()

            assertTrue(effect is TaskListEffect.ShowStorageError)
            val storageError = effect as TaskListEffect.ShowStorageError
            assertEquals(UiText.Res(R.string.storage_error_message), storageError.message)
            assertEquals(
                TaskRetryOperation.ToggleCompletion("id-1", isCompleted = true),
                storageError.operation,
            )
        }

        repository.failSetCompleted = false
        viewModel.onAction(
            TaskListAction.RetryRequested(
                TaskRetryOperation.ToggleCompletion("id-1", isCompleted = true),
            ),
        )
        testScheduler.advanceUntilIdle()

        assertEquals(2, repository.setCompletedCalls)
        assertEquals(5_000L, repository.tasks.value.single().completedAt)
    }
}
