package com.foli.listadetarefas.ui.feature.editor.presentation

import androidx.lifecycle.SavedStateHandle
import com.foli.listadetarefas.core.analytics.NoOpAnalyticsTracker
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.usecase.label.CreateLabelUseCase
import com.foli.listadetarefas.domain.usecase.label.ObserveLabelsUseCase
import com.foli.listadetarefas.domain.usecase.task.CreateTaskUseCase
import com.foli.listadetarefas.domain.usecase.task.ObserveTaskUseCase
import com.foli.listadetarefas.domain.usecase.task.UpdateTaskUseCase
import com.foli.listadetarefas.testing.FakeLabelRepository
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TaskEditorDeadlineTest {

    private val today = LocalDate.of(2026, 10, 30)
    private val time = FakeTimeProvider(nowMillisValue = 500L, todayValue = today)

    private fun createViewModel(
        tasks: FakeTaskRepository,
        taskId: String? = null,
    ) = TaskEditorViewModel(
        savedStateHandle = if (taskId == null) {
            SavedStateHandle()
        } else {
            SavedStateHandle(mapOf("taskId" to taskId))
        },
        createTaskUseCase = CreateTaskUseCase(tasks, time),
        updateTaskUseCase = UpdateTaskUseCase(tasks),
        observeTaskUseCase = ObserveTaskUseCase(tasks),
        observeLabelsUseCase = ObserveLabelsUseCase(FakeLabelRepository()),
        createLabelUseCase = CreateLabelUseCase(FakeLabelRepository(), time),
        timeProvider = time,
        analyticsTracker = NoOpAnalyticsTracker(),
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun state_exposesTodayFromTimeProvider() {
        val viewModel = createViewModel(FakeTaskRepository())

        assertEquals(today, viewModel.state.value.today)
    }

    @Test
    fun create_persistsSelectedDueDate() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val tasks = FakeTaskRepository()
        val viewModel = createViewModel(tasks)
        val dueDate = LocalDate.of(2026, 11, 12)

        viewModel.onAction(TaskEditorAction.TitleChanged("Nova"))
        viewModel.onAction(TaskEditorAction.DueDateSelected(dueDate))
        viewModel.onAction(TaskEditorAction.SaveClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(dueDate, tasks.tasks.value.single().dueDate)
    }

    @Test
    fun edit_loadsDueDateAndCanRemoveIt() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val dueDate = LocalDate.of(2026, 11, 12)
        val tasks = FakeTaskRepository().apply {
            tasks.value = listOf(
                Task(
                    id = "t1",
                    title = "Tarefa",
                    description = "",
                    dueDate = dueDate,
                    createdAt = 1L,
                ),
            )
        }
        val viewModel = createViewModel(tasks, taskId = "t1")
        testScheduler.advanceUntilIdle()

        assertEquals(dueDate, viewModel.state.value.dueDate)

        viewModel.onAction(TaskEditorAction.DueDateRemoved)
        viewModel.onAction(TaskEditorAction.SaveClicked)
        testScheduler.advanceUntilIdle()

        assertNull(tasks.tasks.value.single().dueDate)
        assertEquals(1L, tasks.tasks.value.single().createdAt)
    }

    @Test
    fun pastDate_isAccepted() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val tasks = FakeTaskRepository()
        val viewModel = createViewModel(tasks)
        val past = today.minusDays(5)

        viewModel.onAction(TaskEditorAction.TitleChanged("Atrasada"))
        viewModel.onAction(TaskEditorAction.DueDateSelected(past))
        viewModel.onAction(TaskEditorAction.SaveClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(past, tasks.tasks.value.single().dueDate)
    }
}
