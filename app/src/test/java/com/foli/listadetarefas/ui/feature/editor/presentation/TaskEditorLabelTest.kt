package com.foli.listadetarefas.ui.feature.editor.presentation

import androidx.lifecycle.SavedStateHandle
import com.foli.listadetarefas.R
import com.foli.listadetarefas.core.analytics.NoOpAnalyticsTracker
import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.model.TaskLabel
import com.foli.listadetarefas.domain.usecase.label.CreateLabelUseCase
import com.foli.listadetarefas.domain.usecase.label.ObserveLabelsUseCase
import com.foli.listadetarefas.domain.usecase.task.CreateTaskUseCase
import com.foli.listadetarefas.domain.usecase.task.ObserveTaskUseCase
import com.foli.listadetarefas.domain.usecase.task.UpdateTaskUseCase
import com.foli.listadetarefas.testing.FakeLabelRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskEditorLabelTest {

    private val time = FakeTimeProvider(nowMillisValue = 500L)

    private fun createViewModel(
        tasks: FakeTaskRepository,
        labels: FakeLabelRepository,
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
        observeLabelsUseCase = ObserveLabelsUseCase(labels),
        createLabelUseCase = CreateLabelUseCase(labels, time),
        timeProvider = time,
        analyticsTracker = NoOpAnalyticsTracker(),
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsAvailableLabels_andPreselectsCurrentTaskLabelOnEdit() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val labels = FakeLabelRepository().apply {
            labels.value = listOf(
                TaskLabel("l1", "Trabalho", "trabalho", LabelColorKey.BLUE, 1L),
            )
        }
        val tasks = FakeTaskRepository().apply {
            tasks.value = listOf(
                Task("t1", "Tarefa", "", labelId = "l1", createdAt = 1L),
            )
        }
        val viewModel = createViewModel(tasks, labels, taskId = "t1")
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.state.value.availableLabels.size)
        assertEquals("l1", viewModel.state.value.selectedLabelId)
    }

    @Test
    fun selectingLabel_persistsOnCreate() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val labels = FakeLabelRepository().apply {
            labels.value = listOf(
                TaskLabel("l1", "Trabalho", "trabalho", LabelColorKey.BLUE, 1L),
            )
        }
        val tasks = FakeTaskRepository()
        val viewModel = createViewModel(tasks, labels)

        viewModel.onAction(TaskEditorAction.TitleChanged("Nova"))
        viewModel.onAction(TaskEditorAction.LabelSelected("l1"))
        viewModel.onAction(TaskEditorAction.SaveClicked)
        testScheduler.advanceUntilIdle()

        assertEquals("l1", tasks.tasks.value.single().labelId)
    }

    @Test
    fun removingLabel_onExistingTask_persistsNull() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val labels = FakeLabelRepository().apply {
            labels.value = listOf(
                TaskLabel("l1", "Trabalho", "trabalho", LabelColorKey.BLUE, 1L),
            )
        }
        val tasks = FakeTaskRepository().apply {
            tasks.value = listOf(
                Task("t1", "Tarefa", "", labelId = "l1", createdAt = 1L),
            )
        }
        val viewModel = createViewModel(tasks, labels, taskId = "t1")
        testScheduler.advanceUntilIdle()

        viewModel.onAction(TaskEditorAction.LabelSelected(null))
        viewModel.onAction(TaskEditorAction.SaveClicked)
        testScheduler.advanceUntilIdle()

        assertNull(tasks.tasks.value.single().labelId)
    }

    @Test
    fun createShortcut_preservesFormAndSelectsNewLabel() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val labels = FakeLabelRepository()
        val tasks = FakeTaskRepository()
        val viewModel = createViewModel(tasks, labels)

        viewModel.onAction(TaskEditorAction.TitleChanged("Título digitado"))
        viewModel.onAction(TaskEditorAction.LabelCreateShortcutClicked)
        assertEquals(LabelColorKey.RED, viewModel.state.value.labelEditor?.colorKey)

        viewModel.onAction(TaskEditorAction.LabelEditorNameChanged("  Estudos  "))
        viewModel.onAction(TaskEditorAction.LabelEditorColorSelected(LabelColorKey.GREEN))
        viewModel.onAction(TaskEditorAction.LabelEditorSaveClicked)
        testScheduler.advanceUntilIdle()

        val created = labels.labels.value.single()
        assertEquals("Estudos", created.name)
        assertEquals("Título digitado", viewModel.state.value.title)
        assertEquals(created.id, viewModel.state.value.selectedLabelId)
        assertNull(viewModel.state.value.labelEditor)
    }

    @Test
    fun createShortcut_duplicateKeepsSheetOpenWithInlineError() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val labels = FakeLabelRepository().apply {
            labels.value = listOf(
                TaskLabel("l1", "Trabalho", "trabalho", LabelColorKey.RED, 1L),
            )
        }
        val viewModel = createViewModel(FakeTaskRepository(), labels)

        viewModel.onAction(TaskEditorAction.LabelCreateShortcutClicked)
        viewModel.onAction(TaskEditorAction.LabelEditorNameChanged("TRABALHO"))
        viewModel.onAction(TaskEditorAction.LabelEditorSaveClicked)
        testScheduler.advanceUntilIdle()

        val editor = viewModel.state.value.labelEditor
        assertEquals(UiText.Res(R.string.label_name_duplicate), editor?.nameError)
        assertEquals("TRABALHO", editor?.name)
        assertEquals(1, labels.labels.value.size)
    }

    @Test
    fun labelReadFailure_exposesRetryFlag() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val labels = FakeLabelRepository().apply { failRead = true }
        val viewModel = createViewModel(FakeTaskRepository(), labels)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.labelsLoadFailed)

        labels.failRead = false
        viewModel.onAction(TaskEditorAction.RetryLoadLabelsClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(false, viewModel.state.value.labelsLoadFailed)
    }
}
