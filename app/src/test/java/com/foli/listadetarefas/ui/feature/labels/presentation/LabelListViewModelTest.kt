package com.foli.listadetarefas.ui.feature.labels.presentation

import app.cash.turbine.test
import com.foli.listadetarefas.R
import com.foli.listadetarefas.core.analytics.NoOpAnalyticsTracker
import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.domain.model.TaskLabel
import com.foli.listadetarefas.domain.usecase.label.CreateLabelUseCase
import com.foli.listadetarefas.domain.usecase.label.DeleteLabelUseCase
import com.foli.listadetarefas.domain.usecase.label.ObserveLabelsUseCase
import com.foli.listadetarefas.domain.usecase.label.UpdateLabelUseCase
import com.foli.listadetarefas.testing.FakeLabelRepository
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
class LabelListViewModelTest {

    private fun label(id: String, name: String = "Trabalho", colorKey: LabelColorKey = LabelColorKey.RED) =
        TaskLabel(id, name, name.lowercase(), colorKey, 1L)

    private fun createViewModel(repository: FakeLabelRepository) = LabelListViewModel(
        observeLabelsUseCase = ObserveLabelsUseCase(repository),
        createLabelUseCase = CreateLabelUseCase(repository, FakeTimeProvider(nowMillisValue = 900L)),
        updateLabelUseCase = UpdateLabelUseCase(repository),
        deleteLabelUseCase = DeleteLabelUseCase(repository),
        analyticsTracker = NoOpAnalyticsTracker(),
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun create_opensEditorWithRedPreselectedAndValidatesName() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeLabelRepository()
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(LabelListAction.CreateClicked)
        assertEquals(LabelColorKey.RED, viewModel.state.value.editor?.colorKey)

        viewModel.onAction(LabelListAction.EditorSaveClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(UiText.Res(R.string.label_name_required), viewModel.state.value.editor?.nameError)
        assertTrue(repository.labels.value.isEmpty())
    }

    @Test
    fun create_persistsAndClosesEditor() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeLabelRepository()
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(LabelListAction.CreateClicked)
        viewModel.onAction(LabelListAction.EditorNameChanged("  Trabalho  "))
        viewModel.onAction(LabelListAction.EditorColorSelected(LabelColorKey.BLUE))
        viewModel.onAction(LabelListAction.EditorSaveClicked)
        testScheduler.advanceUntilIdle()

        val saved = repository.labels.value.single()
        assertEquals("Trabalho", saved.name)
        assertEquals(LabelColorKey.BLUE, saved.colorKey)
        assertNull(viewModel.state.value.editor)
        assertEquals(1, viewModel.state.value.labels.size)
    }

    @Test
    fun duplicate_keepsSheetOpenWithInlineErrorAndTypedName() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeLabelRepository().apply {
            labels.value = listOf(label("id-1", "Trabalho"))
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(LabelListAction.CreateClicked)
        viewModel.onAction(LabelListAction.EditorNameChanged("TRABALHO"))
        viewModel.onAction(LabelListAction.EditorSaveClicked)
        testScheduler.advanceUntilIdle()

        val editor = viewModel.state.value.editor
        assertEquals(UiText.Res(R.string.label_name_duplicate), editor?.nameError)
        assertEquals("TRABALHO", editor?.name)
        assertEquals(1, repository.labels.value.size)
    }

    @Test
    fun edit_loadsCurrentValuesAndPersistsChange() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeLabelRepository().apply {
            labels.value = listOf(label("id-1", "Trabalho", LabelColorKey.RED))
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(LabelListAction.LabelClicked("id-1"))
        assertEquals("Trabalho", viewModel.state.value.editor?.name)

        viewModel.onAction(LabelListAction.EditorNameChanged("Pessoal"))
        viewModel.onAction(LabelListAction.EditorColorSelected(LabelColorKey.GREEN))
        viewModel.onAction(LabelListAction.EditorSaveClicked)
        testScheduler.advanceUntilIdle()

        val saved = repository.labels.value.single()
        assertEquals("Pessoal", saved.name)
        assertEquals(LabelColorKey.GREEN, saved.colorKey)
        assertEquals(1L, saved.createdAt)
    }

    @Test
    fun delete_requiresConfirmationAndClearsPending() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeLabelRepository().apply {
            labels.value = listOf(label("id-1"))
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(LabelListAction.DeleteRequested("id-1"))
        assertEquals("id-1", viewModel.state.value.pendingDelete?.id)
        assertEquals(0, repository.deleteCalls)

        viewModel.onAction(LabelListAction.DeleteConfirmed)
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.state.value.pendingDelete)
        assertEquals(1, repository.deleteCalls)
        assertTrue(repository.labels.value.isEmpty())
    }

    @Test
    fun deleteFailure_emitsRetryWithSameLabel() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeLabelRepository().apply {
            labels.value = listOf(label("id-1"))
            failDelete = true
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onAction(LabelListAction.DeleteRequested("id-1"))
            viewModel.onAction(LabelListAction.DeleteConfirmed)
            val effect = awaitItem() as LabelListEffect.ShowStorageError
            assertEquals(LabelRetryOperation.DeleteLabel("id-1"), effect.operation)
        }

        repository.failDelete = false
        viewModel.onAction(LabelListAction.RetryRequested(LabelRetryOperation.DeleteLabel("id-1")))
        testScheduler.advanceUntilIdle()
        assertEquals(2, repository.deleteCalls)
    }

    @Test
    fun readFailure_setsLoadFailedAndRetryRecovers() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeLabelRepository().apply { failRead = true }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.loadFailed)

        repository.failRead = false
        repository.labels.value = listOf(label("id-1"))
        viewModel.onAction(LabelListAction.RetryLoadClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.state.value.labels.size)
        assertEquals(false, viewModel.state.value.loadFailed)
    }
}
