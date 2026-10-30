package com.foli.listadetarefas.ui.feature.tasks.presentation

import androidx.lifecycle.SavedStateHandle

import app.cash.turbine.test
import com.foli.listadetarefas.core.analytics.NoOpAnalyticsTracker
import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.core.time.TimeSignals
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.model.TaskLabel
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
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListLabelTest {

    private fun createViewModel(
        tasks: FakeTaskRepository,
        labels: FakeLabelRepository = FakeLabelRepository(),
        preferences: FakePreferencesRepository = FakePreferencesRepository(),
    ) = TaskListViewModel(
        observeTasksUseCase = ObserveTasksUseCase(tasks),
        observeLabelsUseCase = ObserveLabelsUseCase(labels),
        deleteTaskUseCase = DeleteTaskUseCase(tasks),
        setTaskCompletedUseCase = SetTaskCompletedUseCase(tasks, FakeTimeProvider()),
        observeUserPreferencesUseCase = ObserveUserPreferencesUseCase(preferences),
        setSelectedTabUseCase = SetSelectedTabUseCase(preferences),
        timeProvider = FakeTimeProvider(),
        timeSignals = TimeSignals(),
        analyticsTracker = NoOpAnalyticsTracker(),
        savedStateHandle = SavedStateHandle(),
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun row_exposesLabelNameAndColor_whenTaskHasLabel() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val tasks = FakeTaskRepository().apply {
            tasks.value = listOf(
                Task("t1", "Comprar leite", "", labelId = "l1", createdAt = 1L),
            )
        }
        val labels = FakeLabelRepository().apply {
            labels.value = listOf(TaskLabel("l1", "Trabalho", "trabalho", LabelColorKey.BLUE, 1L))
        }
        val viewModel = createViewModel(tasks, labels)
        testScheduler.advanceUntilIdle()

        val row = viewModel.state.value.rows.single()
        assertEquals("Trabalho", row.label?.name)
        assertEquals(LabelColorKey.BLUE, row.label?.colorKey)
    }

    @Test
    fun row_hasNoLabel_whenTaskIsUnlabeled() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val tasks = FakeTaskRepository().apply {
            tasks.value = listOf(Task("t1", "Sem etiqueta", "", createdAt = 1L))
        }
        val viewModel = createViewModel(tasks)
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.state.value.rows.single().label)
    }

    @Test
    fun renamingLabel_reflectsImmediatelyInTaskRow() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val tasks = FakeTaskRepository().apply {
            tasks.value = listOf(
                Task("t1", "Tarefa", "", labelId = "l1", createdAt = 1L),
            )
        }
        val labels = FakeLabelRepository().apply {
            labels.value = listOf(TaskLabel("l1", "Trabalho", "trabalho", LabelColorKey.RED, 1L))
        }
        val viewModel = createViewModel(tasks, labels)
        testScheduler.advanceUntilIdle()

        labels.labels.value = listOf(
            TaskLabel("l1", "Pessoal", "pessoal", LabelColorKey.GREEN, 1L),
        )
        testScheduler.advanceUntilIdle()

        val row = viewModel.state.value.rows.single()
        assertEquals("Pessoal", row.label?.name)
        assertEquals(LabelColorKey.GREEN, row.label?.colorKey)
    }

    @Test
    fun labelsAction_emitsNavigateToLabels() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = createViewModel(FakeTaskRepository())

        viewModel.effects.test {
            viewModel.onAction(TaskListAction.LabelsClicked)
            assertEquals(TaskListEffect.NavigateToLabels, awaitItem())
        }
    }
}
