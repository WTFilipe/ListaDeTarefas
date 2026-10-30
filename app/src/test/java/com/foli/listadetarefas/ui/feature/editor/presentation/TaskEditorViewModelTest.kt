package com.foli.listadetarefas.ui.feature.editor.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.foli.listadetarefas.R
import com.foli.listadetarefas.core.analytics.NoOpAnalyticsTracker
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.model.TaskLimits
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
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskEditorViewModelTest {

    private val timeProvider = FakeTimeProvider(nowMillisValue = 500L)

    private fun createViewModel(
        repository: FakeTaskRepository,
        taskId: String? = null,
        labelRepository: FakeLabelRepository = FakeLabelRepository(),
    ): TaskEditorViewModel {
        val savedStateHandle = if (taskId == null) {
            SavedStateHandle()
        } else {
            SavedStateHandle(mapOf("taskId" to taskId))
        }
        return TaskEditorViewModel(
            savedStateHandle = savedStateHandle,
            createTaskUseCase = CreateTaskUseCase(repository, timeProvider),
            updateTaskUseCase = UpdateTaskUseCase(repository),
            observeTaskUseCase = ObserveTaskUseCase(repository),
            observeLabelsUseCase = ObserveLabelsUseCase(labelRepository),
            createLabelUseCase = CreateLabelUseCase(labelRepository, timeProvider),
            analyticsTracker = NoOpAnalyticsTracker(),
        )
    }

    private fun existingTask(id: String, title: String = "Título atual") = Task(
        id = id,
        title = title,
        description = "Descrição atual",
        createdAt = 100L,
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveBlankTitle_keepsFormOpenRequestsFocusAndDoesNotWrite() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository()
        val viewModel = createViewModel(repository)

        viewModel.onAction(TaskEditorAction.TitleChanged("   \n "))
        viewModel.effects.test {
            viewModel.onAction(TaskEditorAction.SaveClicked)
            assertEquals(
                TaskEditorEffect.RequestFieldFocus(EditorField.TITLE),
                awaitItem(),
            )
        }
        testScheduler.advanceUntilIdle()

        assertEquals(UiText.Res(R.string.task_title_required), viewModel.state.value.titleError)
        assertEquals("   \n ", viewModel.state.value.title)
        assertEquals(0, repository.upsertCalls)
        assertTrue(repository.tasks.value.isEmpty())
    }

    @Test
    fun saveValid_trimsFieldsAndNavigatesBack() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository()
        val viewModel = createViewModel(repository)

        viewModel.onAction(TaskEditorAction.TitleChanged("  Comprar leite  "))
        viewModel.onAction(TaskEditorAction.DescriptionChanged("     "))
        viewModel.effects.test {
            viewModel.onAction(TaskEditorAction.SaveClicked)
            assertEquals(TaskEditorEffect.NavigateBack, awaitItem())
        }
        testScheduler.advanceUntilIdle()

        val saved = repository.tasks.value.single()
        assertEquals("Comprar leite", saved.title)
        assertEquals("", saved.description)
        assertEquals(timeProvider.nowMillisValue, saved.createdAt)
    }

    @Test
    fun createId_isGeneratedOnceSoRetryDoesNotDuplicate() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply { failUpsert = true }
        val viewModel = createViewModel(repository)
        val createId = (viewModel.state.value.mode as EditorMode.Create).taskId

        viewModel.onAction(TaskEditorAction.TitleChanged("Título"))
        viewModel.onAction(TaskEditorAction.SaveClicked)
        testScheduler.advanceUntilIdle()
        assertNotNull(viewModel.state.value.bannerError)

        repository.failUpsert = false
        viewModel.effects.test {
            viewModel.onAction(TaskEditorAction.RetrySaveClicked)
            assertEquals(TaskEditorEffect.NavigateBack, awaitItem())
        }
        testScheduler.advanceUntilIdle()

        assertEquals(1, repository.tasks.value.size)
        assertEquals(createId, repository.tasks.value.single().id)
        assertEquals(2, repository.upsertCalls)
    }

    @Test
    fun saveFailure_preservesTypedContentAndFlag() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply { failUpsert = true }
        val viewModel = createViewModel(repository)

        viewModel.onAction(TaskEditorAction.TitleChanged("Título digitado"))
        viewModel.onAction(TaskEditorAction.DescriptionChanged("Descrição digitada"))
        viewModel.onAction(TaskEditorAction.SaveClicked)
        testScheduler.advanceUntilIdle()

        assertEquals("Título digitado", viewModel.state.value.title)
        assertEquals("Descrição digitada", viewModel.state.value.description)
        assertEquals(UiText.Res(R.string.storage_error_inline), viewModel.state.value.bannerError)
        assertFalse(viewModel.state.value.isSaving)
    }

    @Test
    fun editLoadsCurrentDataAndSavingKeepsCreatedAt() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(existingTask("id-1"))
        }
        val viewModel = createViewModel(repository, taskId = "id-1")
        testScheduler.advanceUntilIdle()

        assertEquals("Título atual", viewModel.state.value.title)
        assertEquals("Descrição atual", viewModel.state.value.description)

        viewModel.onAction(TaskEditorAction.TitleChanged("Título novo"))
        viewModel.effects.test {
            viewModel.onAction(TaskEditorAction.SaveClicked)
            assertEquals(TaskEditorEffect.NavigateBack, awaitItem())
        }
        testScheduler.advanceUntilIdle()

        val stored = repository.tasks.value.single()
        assertEquals("Título novo", stored.title)
        assertEquals(100L, stored.createdAt)
    }

    @Test
    fun editDeletedTask_navigatesBack() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = createViewModel(FakeTaskRepository(), taskId = "ghost")

        viewModel.effects.test {
            testScheduler.advanceUntilIdle()
            assertEquals(TaskEditorEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun editLoadFailure_setsFlagAndRetryLoadsTask() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply { failRead = true }
        repository.tasks.value = listOf(existingTask("id-1"))
        val viewModel = createViewModel(repository, taskId = "id-1")
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.taskLoadFailed)

        repository.failRead = false
        viewModel.onAction(TaskEditorAction.RetryLoadTaskClicked)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.taskLoadFailed)
        assertEquals("Título atual", viewModel.state.value.title)
    }

    @Test
    fun editLoadFailure_blocksSaveAndRetryDoesNotOverwriteTypedContent() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply { failRead = true }
        repository.tasks.value = listOf(existingTask("id-1"))
        val viewModel = createViewModel(repository, taskId = "id-1")
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.taskLoadFailed)

        // Mesmo com ação de salvar, nada é gravado enquanto a leitura falhou.
        viewModel.onAction(TaskEditorAction.TitleChanged("Rascunho local"))
        viewModel.onAction(TaskEditorAction.SaveClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(0, repository.upsertCalls)
        assertNull(viewModel.state.value.bannerError)

        repository.failRead = false
        viewModel.onAction(TaskEditorAction.RetryLoadTaskClicked)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.taskLoadFailed)
        assertFalse(viewModel.state.value.isLoading)
        // Conteúdo digitado não é sobrescrito pela emissão tardia.
        assertEquals("Rascunho local", viewModel.state.value.title)
    }

    @Test
    fun typingAndCancel_doNotWriteAnything() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository()
        val viewModel = createViewModel(repository)

        viewModel.onAction(TaskEditorAction.TitleChanged("Rascunho"))
        viewModel.onAction(TaskEditorAction.DescriptionChanged("Conteúdo"))
        testScheduler.advanceUntilIdle()

        assertEquals(0, repository.upsertCalls)
        assertNull(viewModel.state.value.bannerError)
        assertNull(viewModel.state.value.titleError)
    }

    @Test
    fun typingAboveTitleLimit_isBlockedAndCounterValueStaysAtMax() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = createViewModel(FakeTaskRepository())
        val tooLong = "a".repeat(TaskLimits.TITLE_MAX + 10)

        viewModel.onAction(TaskEditorAction.TitleChanged(tooLong))
        testScheduler.advanceUntilIdle()

        assertEquals(TaskLimits.TITLE_MAX, viewModel.state.value.title.length)
        assertEquals(tooLong.take(TaskLimits.TITLE_MAX), viewModel.state.value.title)
        assertNull(viewModel.state.value.titleError)
    }

    @Test
    fun typingAboveDescriptionLimit_isBlockedAndCounterValueStaysAtMax() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = createViewModel(FakeTaskRepository())
        val tooLong = "b".repeat(TaskLimits.DESCRIPTION_MAX + 10)

        viewModel.onAction(TaskEditorAction.DescriptionChanged(tooLong))
        testScheduler.advanceUntilIdle()

        assertEquals(TaskLimits.DESCRIPTION_MAX, viewModel.state.value.description.length)
        assertEquals(tooLong.take(TaskLimits.DESCRIPTION_MAX), viewModel.state.value.description)
    }

    @Test
    fun pastingTextAboveLimit_keepsOnlyAcceptedPrefix() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = createViewModel(FakeTaskRepository())
        val pastedTitle = "Título ".repeat(30)
        val pastedDescription = "Descrição ".repeat(300)

        viewModel.onAction(TaskEditorAction.TitleChanged(pastedTitle))
        viewModel.onAction(TaskEditorAction.DescriptionChanged(pastedDescription))
        testScheduler.advanceUntilIdle()

        assertEquals(pastedTitle.take(TaskLimits.TITLE_MAX), viewModel.state.value.title)
        assertEquals(
            pastedDescription.take(TaskLimits.DESCRIPTION_MAX),
            viewModel.state.value.description,
        )
    }

    @Test
    fun editingTaskAboveLimit_normalizesLoadedContent() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val oversized = Task(
            id = "id-1",
            title = "t".repeat(TaskLimits.TITLE_MAX + 5),
            description = "d".repeat(TaskLimits.DESCRIPTION_MAX + 5),
            createdAt = 100L,
        )
        val repository = FakeTaskRepository().apply { tasks.value = listOf(oversized) }

        val viewModel = createViewModel(repository, taskId = "id-1")
        testScheduler.advanceUntilIdle()

        assertEquals(TaskLimits.TITLE_MAX, viewModel.state.value.title.length)
        assertEquals(TaskLimits.DESCRIPTION_MAX, viewModel.state.value.description.length)
    }
}
