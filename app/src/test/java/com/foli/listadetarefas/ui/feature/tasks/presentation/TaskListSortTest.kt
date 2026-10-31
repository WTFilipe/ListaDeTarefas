package com.foli.listadetarefas.ui.feature.tasks.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.foli.listadetarefas.R
import com.foli.listadetarefas.core.analytics.AnalyticsEvents
import com.foli.listadetarefas.core.analytics.AnalyticsTracker
import com.foli.listadetarefas.core.analytics.NoOpAnalyticsTracker
import com.foli.listadetarefas.core.time.TimeSignals
import com.foli.listadetarefas.domain.model.SortOrder
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.model.TaskTab
import com.foli.listadetarefas.domain.model.UserPreferences
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
import com.foli.listadetarefas.ui.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Ordenação da S1 (US-07) no nível de estado: padrão por criação, aplicação
 * imediata preservando a busca, persistência/restauração, consistência entre
 * abas e telemetria sem PII.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskListSortTest {

    private val today = LocalDate.of(2026, 10, 30)

    private fun task(
        id: String,
        title: String,
        createdAt: Long,
        completedAt: Long? = null,
        dueDate: LocalDate? = null,
    ) = Task(
        id = id,
        title = title,
        description = "",
        createdAt = createdAt,
        completedAt = completedAt,
        dueDate = dueDate,
    )

    private fun createViewModel(
        repository: FakeTaskRepository,
        preferences: FakePreferencesRepository = FakePreferencesRepository(),
        analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        timeSignals: TimeSignals = TimeSignals(),
    ) = TaskListViewModel(
        observeTasksUseCase = ObserveTasksUseCase(repository),
        observeLabelsUseCase = ObserveLabelsUseCase(FakeLabelRepository()),
        deleteTaskUseCase = DeleteTaskUseCase(repository),
        setTaskCompletedUseCase = SetTaskCompletedUseCase(repository, FakeTimeProvider()),
        observeUserPreferencesUseCase = ObserveUserPreferencesUseCase(preferences),
        setSelectedTabUseCase = SetSelectedTabUseCase(preferences),
        setSortOrderUseCase = SetSortOrderUseCase(preferences),
        timeProvider = FakeTimeProvider(todayValue = today),
        timeSignals = timeSignals,
        analyticsTracker = analyticsTracker,
        savedStateHandle = savedStateHandle,
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun firstOpening_usesCreationMostRecentFirst() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                task("old", "Antiga", 100L),
                task("new", "Recente", 300L),
                task("middle", "Meio", 200L),
            )
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        assertEquals(SortOrder.CREATED_DESC, viewModel.state.value.sortOrder)
        assertEquals(listOf("new", "middle", "old"), viewModel.state.value.rows.map { it.id })
    }

    @Test
    fun persistedSortOrder_isRestoredAndApplied() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val preferences = FakePreferencesRepository().apply {
            this.preferences.value = UserPreferences(sortOrder = SortOrder.TITLE_DESC)
        }
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                task("z", "Zebra", 100L),
                task("a", "água", 300L),
                task("m", "Mercado", 200L),
            )
        }
        val viewModel = createViewModel(repository, preferences)
        testScheduler.advanceUntilIdle()

        assertEquals(SortOrder.TITLE_DESC, viewModel.state.value.sortOrder)
        assertEquals(listOf("z", "m", "a"), viewModel.state.value.rows.map { it.id })
    }

    @Test
    fun sortOrderSelected_reordersImmediatelyWithoutLosingSearch() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                task("z", "Zebra", 300L),
                task("a", "água", 100L),
                task("m", "Mercado", 200L),
            )
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(TaskListAction.QueryChanged("a"))
        assertEquals(listOf("z", "m", "a"), viewModel.state.value.rows.map { it.id })

        viewModel.onAction(TaskListAction.SortOrderSelected(SortOrder.TITLE_ASC))

        assertEquals("a", viewModel.state.value.query)
        assertEquals(SortOrder.TITLE_ASC, viewModel.state.value.sortOrder)
        assertEquals(listOf("a", "m", "z"), viewModel.state.value.rows.map { it.id })

        viewModel.onAction(TaskListAction.SortOrderSelected(SortOrder.TITLE_DESC))
        testScheduler.advanceUntilIdle()

        assertEquals("a", viewModel.state.value.query)
        assertEquals(listOf("z", "m", "a"), viewModel.state.value.rows.map { it.id })
    }

    @Test
    fun sortOrderSelected_persistsChoiceAndTracksEnumOnly() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val tracker = RecordingAnalyticsTracker()
        val preferences = FakePreferencesRepository()
        val viewModel = createViewModel(
            repository = FakeTaskRepository(),
            preferences = preferences,
            analyticsTracker = tracker,
        )
        testScheduler.advanceUntilIdle()

        viewModel.onAction(TaskListAction.SortOrderSelected(SortOrder.DEADLINE_ASC))
        testScheduler.advanceUntilIdle()

        assertEquals(SortOrder.DEADLINE_ASC, preferences.preferences.value.sortOrder)
        assertEquals(1, preferences.setSortOrderCalls)

        val events = tracker.events.filter { it.name == AnalyticsEvents.SORT_CHANGED }
        assertEquals(1, events.size)
        assertEquals(mapOf("sort_order" to "DEADLINE_ASC"), events.single().params)
    }

    @Test
    fun selectingTheSameSortOrder_doesNotPersistAgainNorTrack() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val tracker = RecordingAnalyticsTracker()
        val preferences = FakePreferencesRepository()
        val viewModel = createViewModel(
            repository = FakeTaskRepository(),
            preferences = preferences,
            analyticsTracker = tracker,
        )
        testScheduler.advanceUntilIdle()

        viewModel.onAction(TaskListAction.SortOrderSelected(SortOrder.CREATED_DESC))
        testScheduler.advanceUntilIdle()

        assertEquals(0, preferences.setSortOrderCalls)
        assertTrue(tracker.events.none { it.name == AnalyticsEvents.SORT_CHANGED })
    }

    @Test
    fun chosenSortOrder_survivesViewModelRecreation() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val preferences = FakePreferencesRepository()
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                task("z", "Zebra", 100L),
                task("a", "água", 300L),
            )
        }
        val first = createViewModel(repository, preferences)
        testScheduler.advanceUntilIdle()
        first.onAction(TaskListAction.SortOrderSelected(SortOrder.TITLE_ASC))
        testScheduler.advanceUntilIdle()

        val recreated = createViewModel(repository, preferences)
        testScheduler.advanceUntilIdle()

        assertEquals(SortOrder.TITLE_ASC, recreated.state.value.sortOrder)
        assertEquals(listOf("a", "z"), recreated.state.value.rows.map { it.id })
    }

    @Test
    fun sortOrder_isAppliedToBothTabs() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val preferences = FakePreferencesRepository().apply {
            this.preferences.value = UserPreferences(sortOrder = SortOrder.TITLE_ASC)
        }
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                task("a-z", "Zebra", 100L),
                task("a-a", "Alface", 200L),
                task("c-u", "uva", 300L, completedAt = 400L),
                task("c-b", "Banana", 400L, completedAt = 500L),
            )
        }
        val viewModel = createViewModel(repository, preferences)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("a-a", "a-z"), viewModel.state.value.rows.map { it.id })

        viewModel.onAction(TaskListAction.TabSelected(TaskTab.COMPLETED))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("c-b", "c-u"), viewModel.state.value.rows.map { it.id })
    }

    @Test
    fun newRoomEmission_keepsTheChosenSortOrder() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(task("b", "Beta", 100L))
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(TaskListAction.SortOrderSelected(SortOrder.TITLE_ASC))
        testScheduler.advanceUntilIdle()

        repository.tasks.value = listOf(
            task("b", "Beta", 100L),
            task("a", "Alfa", 500L),
        )
        testScheduler.advanceUntilIdle()

        assertEquals(SortOrder.TITLE_ASC, viewModel.state.value.sortOrder)
        assertEquals(listOf("a", "b"), viewModel.state.value.rows.map { it.id })
    }

    @Test
    fun deadlineSort_putsTasksWithoutDateLastInBothDirections() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                task("none", "Sem data", 500L),
                task("late", "Longe", 100L, dueDate = LocalDate.of(2026, 12, 1)),
                task("soon", "Perto", 200L, dueDate = LocalDate.of(2026, 10, 31)),
            )
        }
        val viewModel = createViewModel(repository)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(TaskListAction.SortOrderSelected(SortOrder.DEADLINE_ASC))
        assertEquals(listOf("soon", "late", "none"), viewModel.state.value.rows.map { it.id })

        viewModel.onAction(TaskListAction.SortOrderSelected(SortOrder.DEADLINE_DESC))
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("late", "soon", "none"), viewModel.state.value.rows.map { it.id })
    }

    // ------------------------------------------------------------------
    // M6 · escolha local protegida contra emissões durante a gravação
    // ------------------------------------------------------------------

    @Test
    fun interleavedEmissionsWhileWriting_doNotRevertTheChosenOrder() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val timeSignals = TimeSignals()
        val preferences = FakePreferencesRepository().apply {
            setSortOrderDelayMillis = 1_000L
        }
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                task("z", "Zebra", 100L),
                task("a", "água", 300L),
            )
        }
        val viewModel = createViewModel(repository, preferences, timeSignals = timeSignals)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(TaskListAction.SortOrderSelected(SortOrder.TITLE_ASC))
        assertEquals(SortOrder.TITLE_ASC, viewModel.state.value.sortOrder)

        // Room emite e o sinal de tempo chega ANTES da gravação terminar: a
        // preferência ainda é a antiga, mas a escolha local deve prevalecer.
        repository.tasks.value = repository.tasks.value + task("m", "Mercado", 200L)
        timeSignals.emit()
        testScheduler.runCurrent()

        assertEquals(SortOrder.TITLE_ASC, viewModel.state.value.sortOrder)
        assertEquals(listOf("a", "m", "z"), viewModel.state.value.rows.map { it.id })

        testScheduler.advanceUntilIdle()

        assertEquals(SortOrder.TITLE_ASC, preferences.preferences.value.sortOrder)
        assertEquals(SortOrder.TITLE_ASC, viewModel.state.value.sortOrder)
        assertEquals(listOf("a", "m", "z"), viewModel.state.value.rows.map { it.id })
    }

    @Test
    fun sortOrderWriteFailure_revertsToPersistedOrderAndOffersRetry() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val preferences = FakePreferencesRepository().apply {
            failSetSortOrder = true
        }
        val repository = FakeTaskRepository().apply {
            tasks.value = listOf(
                task("z", "Zebra", 100L),
                task("a", "água", 300L),
            )
        }
        val viewModel = createViewModel(repository, preferences)
        testScheduler.advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onAction(TaskListAction.SortOrderSelected(SortOrder.TITLE_ASC))
            assertEquals(SortOrder.TITLE_ASC, viewModel.state.value.sortOrder)

            val effect = awaitItem()
            assertTrue(effect is TaskListEffect.ShowStorageError)
            val storageError = effect as TaskListEffect.ShowStorageError
            assertEquals(UiText.Res(R.string.storage_error_message), storageError.message)
            assertEquals(
                TaskRetryOperation.SetSortOrder(SortOrder.TITLE_ASC),
                storageError.operation,
            )
        }

        assertTrue(viewModel.state.value.sortOrder == SortOrder.CREATED_DESC)
        assertEquals(listOf("a", "z"), viewModel.state.value.rows.map { it.id })

        preferences.failSetSortOrder = false
        viewModel.onAction(
            TaskListAction.RetryRequested(TaskRetryOperation.SetSortOrder(SortOrder.TITLE_ASC)),
        )
        testScheduler.advanceUntilIdle()

        assertEquals(SortOrder.TITLE_ASC, viewModel.state.value.sortOrder)
        assertEquals(SortOrder.TITLE_ASC, preferences.preferences.value.sortOrder)
        assertEquals(2, preferences.setSortOrderCalls)
        assertEquals(listOf("a", "z"), viewModel.state.value.rows.map { it.id })
    }
}
