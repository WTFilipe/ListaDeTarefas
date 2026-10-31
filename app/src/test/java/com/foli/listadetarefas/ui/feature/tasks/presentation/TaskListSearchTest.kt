package com.foli.listadetarefas.ui.feature.tasks.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.foli.listadetarefas.core.analytics.AnalyticsEvents
import com.foli.listadetarefas.core.analytics.NoOpAnalyticsTracker
import com.foli.listadetarefas.core.analytics.AnalyticsTracker
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
import com.foli.listadetarefas.testing.RecordingAnalyticsTracker
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

/**
 * Busca da S1 (US-06) no nível de estado: filtro durante a digitação, escopo por
 * aba, restauração ao limpar, preservação ao voltar do formulário/recriar o
 * ViewModel, projeção de novas emissões do Room e telemetria sem PII.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskListSearchTest {

    private fun task(
        id: String,
        title: String,
        description: String = "",
        createdAt: Long = 100L,
        completedAt: Long? = null,
    ) = Task(
        id = id,
        title = title,
        description = description,
        createdAt = createdAt,
        completedAt = completedAt,
    )

    private fun createViewModel(
        repository: FakeTaskRepository,
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
        preferences: FakePreferencesRepository = FakePreferencesRepository(),
    ) = TaskListViewModel(
        observeTasksUseCase = ObserveTasksUseCase(repository),
        observeLabelsUseCase = ObserveLabelsUseCase(FakeLabelRepository()),
        deleteTaskUseCase = DeleteTaskUseCase(repository),
        setTaskCompletedUseCase = SetTaskCompletedUseCase(repository, FakeTimeProvider()),
        observeUserPreferencesUseCase = ObserveUserPreferencesUseCase(preferences),
        setSelectedTabUseCase = SetSelectedTabUseCase(preferences),
        setSortOrderUseCase = SetSortOrderUseCase(preferences),
        timeProvider = FakeTimeProvider(),
        timeSignals = TimeSignals(),
        analyticsTracker = analyticsTracker,
        savedStateHandle = savedStateHandle,
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun queryChanged_filtersRowsDuringTypingWithoutConfirm() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                task("leite", "Comprar leite", createdAt = 300L),
                task("proposta", "Revisar proposta", createdAt = 200L),
            )
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(TaskListAction.QueryChanged("leit"))

            val filtered = awaitItem()
            assertEquals("leit", filtered.query)
            assertEquals(listOf("leite"), filtered.rows.map { it.id })
        }
    }

    @Test
    fun queryChanged_matchesDescriptionIgnoringCaseAndAccents() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                task("arvore", "Estudos", description = "Revisar a árvore genealógica"),
                task("mercado", "Comprar leite"),
            )
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(TaskListAction.QueryChanged("ARVORE"))

        assertEquals(listOf("arvore"), viewModel.state.value.rows.map { it.id })
        assertFalse(viewModel.state.value.rows.single().isCompleted)
    }

    @Test
    fun blankQuery_restoresFullListKeepingCurrentOrder() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                task("old", "Antiga", createdAt = 100L),
                task("new", "Recente", createdAt = 300L),
            )
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(TaskListAction.QueryChanged("recente"))
        assertEquals(listOf("new"), viewModel.state.value.rows.map { it.id })

        viewModel.onAction(TaskListAction.QueryChanged("   "))
        assertEquals(listOf("new", "old"), viewModel.state.value.rows.map { it.id })
    }

    @Test
    fun search_isScopedToVisibleTabAndPreservedOnTabChange() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                task("active-leite", "Comprar leite", createdAt = 300L),
                task("done-leite", "Comprar leite", createdAt = 200L, completedAt = 900L),
            )
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(TaskListAction.QueryChanged("leite"))
        assertEquals(listOf("active-leite"), viewModel.state.value.rows.map { it.id })

        viewModel.onAction(TaskListAction.TabSelected(TaskTab.COMPLETED))
        testScheduler.advanceUntilIdle()

        assertEquals("leite", viewModel.state.value.query)
        assertEquals(listOf("done-leite"), viewModel.state.value.rows.map { it.id })
    }

    @Test
    fun searchCleared_restoresFullListOfCurrentTabAndTracksEvent() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val tracker = RecordingAnalyticsTracker()
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                task("leite", "Comprar leite", createdAt = 300L),
                task("pao", "Comprar pão", createdAt = 200L),
            )
        }
        val viewModel = createViewModel(repository, analyticsTracker = tracker)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(TaskListAction.QueryChanged("leite"))
        viewModel.onAction(TaskListAction.SearchCleared)
        testScheduler.advanceUntilIdle()

        assertEquals("", viewModel.state.value.query)
        assertEquals(listOf("leite", "pao"), viewModel.state.value.rows.map { it.id })

        val cleared = tracker.events.filter { it.name == AnalyticsEvents.SEARCH_CLEARED }
        assertEquals(1, cleared.size)
        assertEquals(mapOf("had_query" to true), cleared.single().params)
    }

    @Test
    fun searchCleared_withoutQueryDoesNotTrackClearedEvent() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val tracker = RecordingAnalyticsTracker()
        val viewModel = createViewModel(FakeTaskRepository(), analyticsTracker = tracker)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(TaskListAction.SearchCleared)
        testScheduler.advanceUntilIdle()

        assertTrue(tracker.events.none { it.name == AnalyticsEvents.SEARCH_CLEARED })
    }

    @Test
    fun querySurvivesViewModelRecreationBackFromTheForm() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                task("leite", "Comprar leite", createdAt = 300L),
                task("pao", "Comprar pão", createdAt = 200L),
            )
        }
        val savedStateHandle = SavedStateHandle()
        val firstViewModel = createViewModel(repository, savedStateHandle)
        testScheduler.advanceUntilIdle()
        firstViewModel.onAction(TaskListAction.QueryChanged("leite"))
        testScheduler.advanceUntilIdle()

        // Volta do formulário/recriação: o mesmo SavedStateHandle é reaproveitado.
        val recreated = createViewModel(repository, savedStateHandle)
        testScheduler.advanceUntilIdle()

        assertEquals("leite", recreated.state.value.query)
        assertEquals(listOf("leite"), recreated.state.value.rows.map { it.id })
    }

    @Test
    fun newRoomEmissions_areProjectedWithTheActiveQuery() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(task("old-leite", "Comprar leite", createdAt = 100L))
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()
        viewModel.onAction(TaskListAction.QueryChanged("leite"))
        assertEquals(listOf("old-leite"), viewModel.state.value.rows.map { it.id })

        // Criação/edição/conclusão refletem no resultado filtrado ao voltar do S2.
        repository.tasks.value = listOf(
            task("old-leite", "Comprar leite", createdAt = 100L),
            task("new-leite", "Comprar leite desnatado", createdAt = 500L),
            task("pao", "Comprar pão", createdAt = 400L),
        )
        testScheduler.advanceUntilIdle()

        assertEquals(
            listOf("new-leite", "old-leite"),
            viewModel.state.value.rows.map { it.id },
        )
    }

    @Test
    fun completingTask_removesItFromTheFilteredActiveResult() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(task("leite", "Comprar leite"))
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()
        viewModel.onAction(TaskListAction.QueryChanged("leite"))

        viewModel.onAction(TaskListAction.CompletionToggled("leite", isCompleted = true))
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.rows.isEmpty())
        assertEquals("leite", viewModel.state.value.query)
    }

    @Test
    fun searchDoesNotWriteCreateOrDeleteData() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(task("leite", "Comprar leite"))
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(TaskListAction.QueryChanged("leite"))
        viewModel.onAction(TaskListAction.QueryChanged("leite d"))
        viewModel.onAction(TaskListAction.SearchCleared)
        testScheduler.advanceUntilIdle()

        assertEquals(0, repository.upsertCalls)
        assertEquals(0, repository.deleteCalls)
        assertEquals(0, repository.setCompletedCalls)
        assertEquals(1, repository.tasks.value.size)
    }

    @Test
    fun searchAnalytics_isDebouncedAndCarriesOnlyResultCount() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val tracker = RecordingAnalyticsTracker()
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                task("leite", "Comprar leite", createdAt = 300L),
                task("pao", "Comprar pão", createdAt = 200L),
            )
        }
        val viewModel = createViewModel(repository, analyticsTracker = tracker)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(TaskListAction.QueryChanged("lei"))
        testScheduler.advanceTimeBy(150)
        viewModel.onAction(TaskListAction.QueryChanged("leite"))
        testScheduler.advanceTimeBy(199)

        assertTrue(tracker.events.none { it.name == AnalyticsEvents.SEARCH_PERFORMED })

        testScheduler.advanceTimeBy(1)
        testScheduler.runCurrent()

        val performed = tracker.events.filter { it.name == AnalyticsEvents.SEARCH_PERFORMED }
        assertEquals(1, performed.size)
        assertEquals(mapOf("result_count" to 1), performed.single().params)
        assertTrue(
            performed.single().params.keys.none { it.contains("query", ignoreCase = true) },
        )
    }

    @Test
    fun clearingSearchCancelsPendingPerformedAnalytics() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val tracker = RecordingAnalyticsTracker()
        val viewModel = createViewModel(FakeTaskRepository(), analyticsTracker = tracker)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(TaskListAction.QueryChanged("leite"))
        viewModel.onAction(TaskListAction.SearchCleared)
        testScheduler.advanceUntilIdle()

        assertTrue(tracker.events.none { it.name == AnalyticsEvents.SEARCH_PERFORMED })
    }
}
