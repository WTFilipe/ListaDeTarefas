package com.foli.listadetarefas.ui.feature.tasks.presentation

import app.cash.turbine.test
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {

    private fun task(id: String, title: String, createdAt: Long) = Task(
        id = id,
        title = title,
        description = "",
        createdAt = createdAt,
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
    fun loadsRowsInCurrentOrder() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository()
        repository.tasks.value = listOf(
            task("old", "Antiga", 100L),
            task("new", "Recente", 300L),
        )
        val viewModel = createViewModel(repository)

        viewModel.state.test {
            awaitItem()
            testScheduler.advanceUntilIdle()
            val loaded = awaitItem()

            assertEquals(listOf("new", "old"), loaded.rows.map { it.id })
            assertFalse(loaded.isLoading)
            assertFalse(loaded.loadFailed)
        }
    }

    @Test
    fun emptyList_isNotLoadingAndNotFailed() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = createViewModel(FakeTaskRepository())

        viewModel.state.test {
            awaitItem()
            testScheduler.advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }

        val loaded = viewModel.state.value
        assertTrue(loaded.rows.isEmpty())
        assertFalse(loaded.isLoading)
        assertFalse(loaded.loadFailed)
    }

    @Test
    fun slowRead_showsLoadingOnlyAfterTwoHundredMillis() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply { readDelayMillis = 500L }
        repository.tasks.value = listOf(task("id-1", "Tarefa", 100L))
        val viewModel = createViewModel(repository)

        viewModel.state.test {
            awaitItem()
            testScheduler.advanceTimeBy(250)
            val loading = awaitItem()
            assertTrue(loading.isLoading)

            testScheduler.advanceTimeBy(300)
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(1, loaded.rows.size)
        }
    }

    @Test
    fun readFailure_setsLoadFailedAndRetryRecovers() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply { failRead = true }
        val viewModel = createViewModel(repository)

        viewModel.state.test {
            awaitItem()
            testScheduler.advanceUntilIdle()
            val failed = awaitItem()
            assertTrue(failed.loadFailed)
            assertTrue(failed.rows.isEmpty())
        }

        repository.failRead = false
        repository.tasks.value = listOf(task("id-1", "Tarefa", 100L))
        viewModel.onAction(TaskListAction.RetryLoadClicked)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.loadFailed)
        assertEquals(listOf("id-1"), viewModel.state.value.rows.map { it.id })
    }

    @Test
    fun newTask_emitsNavigateToEditorWithNullId() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = createViewModel(FakeTaskRepository())

        viewModel.effects.test {
            viewModel.onAction(TaskListAction.NewTaskClicked)
            assertEquals(TaskListEffect.NavigateToEditor(null), awaitItem())
        }
    }

    @Test
    fun taskClick_emitsNavigateToEditorWithTaskId() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = createViewModel(FakeTaskRepository())

        viewModel.effects.test {
            viewModel.onAction(TaskListAction.TaskClicked("id-42"))
            assertEquals(TaskListEffect.NavigateToEditor("id-42"), awaitItem())
        }
    }
}
