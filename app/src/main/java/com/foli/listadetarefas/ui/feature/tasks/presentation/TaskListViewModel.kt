package com.foli.listadetarefas.ui.feature.tasks.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foli.listadetarefas.R
import com.foli.listadetarefas.core.analytics.AnalyticsEvents
import com.foli.listadetarefas.core.analytics.AnalyticsTracker
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.core.time.TimeProvider
import com.foli.listadetarefas.core.time.TimeSignals
import com.foli.listadetarefas.domain.logic.TaskFilter
import com.foli.listadetarefas.domain.logic.TaskSorter
import com.foli.listadetarefas.domain.model.SortOrder
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.model.TaskLabel
import com.foli.listadetarefas.domain.model.TaskTab
import com.foli.listadetarefas.domain.usecase.label.ObserveLabelsUseCase
import com.foli.listadetarefas.domain.usecase.preferences.ObserveUserPreferencesUseCase
import com.foli.listadetarefas.domain.usecase.preferences.SetSelectedTabUseCase
import com.foli.listadetarefas.domain.usecase.preferences.SetSortOrderUseCase
import com.foli.listadetarefas.domain.usecase.task.DeleteTaskUseCase
import com.foli.listadetarefas.domain.usecase.task.ObserveTasksUseCase
import com.foli.listadetarefas.domain.usecase.task.SetTaskCompletedUseCase
import com.foli.listadetarefas.ui.UiText
import com.foli.listadetarefas.ui.model.TaskUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel MVI da S1.
 *
 * - Combina a observação de tarefas (sem ordem própria) com as preferências
 *   persistidas (aba e ordenação) e projeta as linhas com a composição
 *   `aba -> busca -> ordenação` (`ARCHITECTURE.md` §4.1).
 * - A busca (`QueryChanged`/`SearchCleared`) filtra por substring no título ou
 *   na descrição e é reaplicada a **cada** emissão do Room e a cada ação, de modo
 *   que criar, editar, concluir ou reabrir na tela do formulário já reflete no
 *   resultado filtrado ao voltar (US-06 AC 9). O texto digitado fica no
 *   `SavedStateHandle`, sem persistência em disco (ARCHITECTURE §9.2).
 * - `TabSelected` atualiza o estado imediatamente e persiste em DataStore; a
 *   aba selecionada é restaurada ao reabrir o app. Trocar de aba preserva a
 *   consulta, que continua escopada à aba visível.
 * - `SortOrderSelected` reordena as linhas visíveis na hora, sem perder a busca
 *   ativa, persiste a escolha em DataStore (US-07 AC 3/4) e emite
 *   `sort_changed` apenas com o nome do enum (sem PII).
 * - `CompletionToggled` chama `SetTaskCompletedUseCase` (idempotente) e preserva
 *   todos os campos; em falha de armazenamento emite `ShowStorageError` com a
 *   operação exata para "Tentar novamente".
 * - O prazo é reprojetado ao abrir, ao retomar do segundo plano, em sinais de
 *   data/hora/fuso e a cada ação (primeira recomposição após a virada). Não há
 *   timer de meia-noite (US-05 AC 5).
 * - O loading só aparece se a primeira emissão demorar mais de 200 ms
 *   (DESIGN_SPEC §8.2).
 */
@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val observeTasksUseCase: ObserveTasksUseCase,
    private val observeLabelsUseCase: ObserveLabelsUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val setTaskCompletedUseCase: SetTaskCompletedUseCase,
    private val observeUserPreferencesUseCase: ObserveUserPreferencesUseCase,
    private val setSelectedTabUseCase: SetSelectedTabUseCase,
    private val setSortOrderUseCase: SetSortOrderUseCase,
    private val timeProvider: TimeProvider,
    private val timeSignals: TimeSignals,
    private val analyticsTracker: AnalyticsTracker,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(
        TaskListState(query = savedStateHandle.get<String>(KEY_QUERY).orEmpty()),
    )
    val state: StateFlow<TaskListState> = _state.asStateFlow()

    private val _effects = Channel<TaskListEffect>(Channel.BUFFERED)
    val effects: Flow<TaskListEffect> = _effects.receiveAsFlow()

    private var observeJob: Job? = null
    private var searchAnalyticsJob: Job? = null
    private var latestTasks: List<Task> = emptyList()
    private var latestLabels: List<TaskLabel> = emptyList()

    /**
     * Última ordenação **confirmada** pelo DataStore. Serve de valor de reversão
     * quando a escrita da escolha falha (M4) e é atualizada a cada emissão das
     * preferências.
     */
    private var persistedSortOrder: SortOrder = SortOrder.DEFAULT

    /**
     * Escolha local ainda não confirmada pelas preferências (M6). Enquanto ela
     * existir, a projeção usa esta ordem em vez da preferência emitida, para que
     * uma emissão do Room ou de [TimeSignals] entre a ação e a gravação não
     * reverta a lista por um instante.
     */
    private var pendingSortOrder: SortOrder? = null

    init {
        startObserving()
    }

    fun onAction(action: TaskListAction) {
        // Toda ação recalcula o prazo antes de reduzir: é a "primeira
        // recomposição após a virada" prevista na US-05 AC 5.
        refreshDeadlines()
        when (action) {
            is TaskListAction.QueryChanged -> updateQuery(action.value)
            TaskListAction.SearchCleared -> clearSearch()
            is TaskListAction.TabSelected -> selectTab(action.tab)
            is TaskListAction.SortOrderSelected -> selectSortOrder(action.sortOrder)
            is TaskListAction.CompletionToggled ->
                toggleCompletion(action.taskId, action.isCompleted)
            is TaskListAction.TaskClicked -> emitEffect(TaskListEffect.NavigateToEditor(action.taskId))
            TaskListAction.NewTaskClicked -> emitEffect(TaskListEffect.NavigateToEditor(null))
            TaskListAction.LabelsClicked -> emitEffect(TaskListEffect.NavigateToLabels)
            is TaskListAction.DeleteRequested -> _state.update { it.copy(pendingDeleteId = action.taskId) }
            TaskListAction.DeleteDismissed -> _state.update { it.copy(pendingDeleteId = null) }
            TaskListAction.DeleteConfirmed -> confirmDelete()
            TaskListAction.RetryLoadClicked -> startObserving()
            TaskListAction.TimeRefreshRequested -> Unit
            is TaskListAction.RetryRequested -> retry(action.operation)
        }
    }

    private fun startObserving() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _state.update { it.copy(loadFailed = false, isLoading = false) }
            var firstEmissionReceived = false
            val slowLoadingJob = launch {
                delay(LOADING_DELAY_MS)
                if (!firstEmissionReceived) {
                    _state.update { it.copy(isLoading = true) }
                }
            }
            combine(
                observeTasksUseCase(),
                observeLabelsUseCase(),
                observeUserPreferencesUseCase(),
                timeSignals.signals.onStart { emit(Unit) },
            ) { tasks, labels, preferences, _ ->
                Triple(tasks, labels, preferences)
            }
                .catch {
                    slowLoadingJob.cancel()
                    _state.update { current -> current.copy(isLoading = false, loadFailed = true) }
                }
                .collect { (tasks, labels, preferences) ->
                    firstEmissionReceived = true
                    slowLoadingJob.cancel()
                    latestTasks = tasks
                    latestLabels = labels
                    persistedSortOrder = preferences.sortOrder
                    val sortOrder = resolveSortOrder(preferences.sortOrder)
                    _state.update { current ->
                        current.copy(
                            tab = preferences.selectedTab,
                            sortOrder = sortOrder,
                            rows = project(
                                tasks = tasks,
                                labels = labels,
                                tab = preferences.selectedTab,
                                query = current.query,
                                sortOrder = sortOrder,
                                today = timeProvider.today(),
                            ),
                            isLoading = false,
                            loadFailed = false,
                        )
                    }
                }
        }
    }

    /**
     * Resolve a ordenação vigente: a escolha local pendente tem prioridade até o
     * DataStore confirmar a persistência (M6); quando a preferência emitida já é
     * a escolha, a pendência é descartada.
     */
    private fun resolveSortOrder(persisted: SortOrder): SortOrder {
        val pending = pendingSortOrder ?: return persisted
        if (pending == persisted) {
            pendingSortOrder = null
            return persisted
        }
        return pending
    }

    private fun project(
        tasks: List<Task>,
        labels: List<TaskLabel>,
        tab: TaskTab,
        query: String,
        sortOrder: SortOrder,
        today: java.time.LocalDate,
    ): List<TaskUiModel> {
        val labelById = labels.associateBy { it.id }
        // Ordem de leitura da lista: aba -> busca -> ordenação vigente
        // (`ARCHITECTURE.md` §4.1). O filtro é estável e não reordena; a
        // ordenação é a única responsável pela ordem final.
        val visible = TaskSorter.sort(
            items = TaskFilter.filterByQuery(TaskFilter.byTab(tasks, tab), query),
            order = sortOrder,
        )
        return visible.map { task ->
            TaskUiMapper.toUiModel(
                task = task,
                label = task.labelId?.let(labelById::get),
                today = today,
            )
        }
    }

    private fun refreshDeadlines() {
        _state.update { current ->
            current.copy(
                rows = project(
                    tasks = latestTasks,
                    labels = latestLabels,
                    tab = current.tab,
                    query = current.query,
                    sortOrder = current.sortOrder,
                    today = timeProvider.today(),
                ),
            )
        }
    }

    private fun updateQuery(value: String) {
        if (_state.value.query == value) return
        savedStateHandle[KEY_QUERY] = value
        _state.update { current ->
            current.copy(
                query = value,
                rows = project(
                    tasks = latestTasks,
                    labels = latestLabels,
                    tab = current.tab,
                    query = value,
                    sortOrder = current.sortOrder,
                    today = timeProvider.today(),
                ),
            )
        }
        scheduleSearchAnalytics(value)
    }

    private fun clearSearch() {
        val hadQuery = _state.value.query.isNotBlank()
        searchAnalyticsJob?.cancel()
        savedStateHandle[KEY_QUERY] = ""
        _state.update { current ->
            current.copy(
                query = "",
                rows = project(
                    tasks = latestTasks,
                    labels = latestLabels,
                    tab = current.tab,
                    query = "",
                    sortOrder = current.sortOrder,
                    today = timeProvider.today(),
                ),
            )
        }
        if (hadQuery) {
            analyticsTracker.track(
                AnalyticsEvents.SEARCH_CLEARED,
                mapOf(ANALYTICS_HAD_QUERY to true),
            )
        }
    }

    /**
     * `search_performed` só é emitido após 200 ms sem novas teclas e carrega
     * apenas a contagem de resultados — nunca o texto consultado
     * (`ARCHITECTURE.md` §10).
     */
    private fun scheduleSearchAnalytics(query: String) {
        searchAnalyticsJob?.cancel()
        if (query.isBlank()) return
        searchAnalyticsJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            analyticsTracker.track(
                AnalyticsEvents.SEARCH_PERFORMED,
                mapOf(ANALYTICS_RESULT_COUNT to _state.value.rows.size),
            )
        }
    }

    private fun selectTab(tab: TaskTab) {
        if (_state.value.tab == tab) return
        _state.update { current ->
            current.copy(
                tab = tab,
                rows = project(
                    tasks = latestTasks,
                    labels = latestLabels,
                    tab = tab,
                    query = current.query,
                    sortOrder = current.sortOrder,
                    today = timeProvider.today(),
                ),
            )
        }
        viewModelScope.launch { setSelectedTabUseCase(tab) }
    }

    /**
     * Troca a ordenação: reordena as linhas visíveis imediatamente, mantendo
     * consulta e aba, e persiste a escolha para sobreviver ao fechamento do app.
     *
     * A escolha entra em [pendingSortOrder] para blindar a lista contra emissões
     * concorrentes até o DataStore confirmar a gravação (M6). Se a escrita falhar,
     * o estado volta para a ordenação realmente persistida e a UI recebe o snackbar
     * com "Tentar novamente", sem encerrar o processo (M4).
     */
    private fun selectSortOrder(order: SortOrder) {
        if (_state.value.sortOrder == order) return
        pendingSortOrder = order
        _state.update { current ->
            current.copy(
                sortOrder = order,
                rows = project(
                    tasks = latestTasks,
                    labels = latestLabels,
                    tab = current.tab,
                    query = current.query,
                    sortOrder = order,
                    today = timeProvider.today(),
                ),
            )
        }
        analyticsTracker.track(
            AnalyticsEvents.SORT_CHANGED,
            mapOf(ANALYTICS_SORT_ORDER to order.name),
        )
        persistSortOrder(order)
    }

    private fun persistSortOrder(order: SortOrder) {
        viewModelScope.launch {
            try {
                setSortOrderUseCase(order)
            } catch (error: CancellationException) {
                throw error
            } catch (@Suppress("TooGenericExceptionCaught") error: Exception) {
                // Erro de I/O do DataStore: descarta a escolha otimista e volta à
                // ordem ainda vigente no disco, oferecendo a repetição da escrita.
                // Se outra escolha mais nova já assumiu, esta falha é ignorada.
                if (pendingSortOrder != order) return@launch
                pendingSortOrder = null
                val restored = persistedSortOrder
                _state.update { current ->
                    current.copy(
                        sortOrder = restored,
                        rows = project(
                            tasks = latestTasks,
                            labels = latestLabels,
                            tab = current.tab,
                            query = current.query,
                            sortOrder = restored,
                            today = timeProvider.today(),
                        ),
                    )
                }
                emitEffect(
                    TaskListEffect.ShowStorageError(
                        message = UiText.Res(R.string.storage_error_message),
                        operation = TaskRetryOperation.SetSortOrder(order),
                    ),
                )
            }
        }
    }

    private fun toggleCompletion(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            when (val result = setTaskCompletedUseCase(taskId, isCompleted)) {
                is AppResult.Success -> analyticsTracker.track(
                    if (isCompleted) AnalyticsEvents.TASK_COMPLETED else AnalyticsEvents.TASK_REOPENED,
                )
                is AppResult.Failure -> emitEffect(
                    TaskListEffect.ShowStorageError(
                        message = UiText.Res(R.string.storage_error_message),
                        operation = TaskRetryOperation.ToggleCompletion(taskId, isCompleted),
                    ),
                )
            }
        }
    }

    private fun confirmDelete() {
        val taskId = _state.value.pendingDeleteId ?: return
        _state.update { it.copy(pendingDeleteId = null) }
        performDelete(taskId)
    }

    private fun performDelete(taskId: String) {
        viewModelScope.launch {
            when (deleteTaskUseCase(taskId)) {
                is AppResult.Success -> analyticsTracker.track(AnalyticsEvents.TASK_DELETED)
                is AppResult.Failure -> emitEffect(
                    TaskListEffect.ShowStorageError(
                        message = UiText.Res(R.string.storage_delete_error_message),
                        operation = TaskRetryOperation.DeleteTask(taskId),
                    ),
                )
            }
        }
    }

    private fun retry(operation: TaskRetryOperation) {
        when (operation) {
            is TaskRetryOperation.DeleteTask -> performDelete(operation.taskId)
            is TaskRetryOperation.ToggleCompletion ->
                toggleCompletion(operation.taskId, operation.isCompleted)
            is TaskRetryOperation.SetSortOrder -> selectSortOrder(operation.sortOrder)
        }
    }

    private fun emitEffect(effect: TaskListEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private companion object {
        const val LOADING_DELAY_MS = 200L
        const val SEARCH_DEBOUNCE_MS = 200L
        const val KEY_QUERY = "task_list_query"
        const val ANALYTICS_RESULT_COUNT = "result_count"
        const val ANALYTICS_HAD_QUERY = "had_query"
        const val ANALYTICS_SORT_ORDER = "sort_order"
    }
}
