package com.foli.listadetarefas.ui.feature.tasks.presentation

import androidx.lifecycle.SavedStateHandle

import com.foli.listadetarefas.core.analytics.NoOpAnalyticsTracker
import com.foli.listadetarefas.core.time.TimeSignals
import com.foli.listadetarefas.domain.model.Task
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
import com.foli.listadetarefas.ui.model.DeadlineUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListDeadlineTest {

    private val today = LocalDate.of(2026, 10, 30)

    private val timeProvider = FakeTimeProvider(todayValue = today)

    private fun createViewModel(repository: FakeTaskRepository) = TaskListViewModel(
        observeTasksUseCase = ObserveTasksUseCase(repository),
        observeLabelsUseCase = ObserveLabelsUseCase(FakeLabelRepository()),
        deleteTaskUseCase = DeleteTaskUseCase(repository),
        setTaskCompletedUseCase = SetTaskCompletedUseCase(repository, timeProvider),
        observeUserPreferencesUseCase = ObserveUserPreferencesUseCase(FakePreferencesRepository()),
        setSelectedTabUseCase = SetSelectedTabUseCase(FakePreferencesRepository()),
        setSortOrderUseCase = SetSortOrderUseCase(FakePreferencesRepository()),
        timeProvider = timeProvider,
        timeSignals = TimeSignals(),
        analyticsTracker = NoOpAnalyticsTracker(),
        savedStateHandle = SavedStateHandle(),
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun rows_carryDeadlineStateAndLabel() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                Task(
                    id = "overdue",
                    title = "Atrasada",
                    description = "",
                    dueDate = today.minusDays(1),
                    createdAt = 1L,
                ),
                Task(
                    id = "soon",
                    title = "Em breve",
                    description = "",
                    dueDate = today.plusDays(7),
                    createdAt = 2L,
                ),
            )
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        val byId = viewModel.state.value.rows.associateBy { it.id }
        assertEquals(DeadlineUiState.OVERDUE, byId.getValue("overdue").deadline)
        assertEquals("29/10/2026", byId.getValue("overdue").deadlineDateLabel)
        assertEquals(DeadlineUiState.SOON, byId.getValue("soon").deadline)
    }

    @Test
    fun actionReevaluatesTodaySoIndicatorUpdatesAfterDayChange() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                Task(
                    id = "id-1",
                    title = "Hoje",
                    description = "",
                    dueDate = today,
                    createdAt = 1L,
                ),
            )
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()
        assertEquals(DeadlineUiState.TODAY, viewModel.state.value.rows.single().deadline)

        timeProvider.todayValue = today.plusDays(1)
        viewModel.onAction(TaskListAction.TimeRefreshRequested)
        testScheduler.advanceUntilIdle()

        assertEquals(DeadlineUiState.OVERDUE, viewModel.state.value.rows.single().deadline)
    }

    @Test
    fun completedTask_hasNoDeadlineIndicator() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                Task(
                    id = "id-1",
                    title = "Concluída",
                    description = "",
                    dueDate = today.minusDays(4),
                    createdAt = 1L,
                    completedAt = 900L,
                ),
            )
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()
        viewModel.onAction(TaskListAction.TabSelected(com.foli.listadetarefas.domain.model.TaskTab.COMPLETED))
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.state.value.rows.single().deadline)
    }
}
