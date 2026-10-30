package com.foli.listadetarefas.ui.feature.tasks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foli.listadetarefas.R
import com.foli.listadetarefas.core.analytics.AnalyticsEvents
import com.foli.listadetarefas.core.analytics.AnalyticsTracker
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.core.time.TimeProvider
import com.foli.listadetarefas.core.time.TimeSignals
import com.foli.listadetarefas.domain.logic.TaskFilter
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.model.TaskLabel
import com.foli.listadetarefas.domain.model.TaskTab
import com.foli.listadetarefas.domain.usecase.label.ObserveLabelsUseCase
import com.foli.listadetarefas.domain.usecase.preferences.ObserveUserPreferencesUseCase
import com.foli.listadetarefas.domain.usecase.preferences.SetSelectedTabUseCase
import com.foli.listadetarefas.domain.usecase.task.DeleteTaskUseCase
import com.foli.listadetarefas.domain.usecase.task.ObserveTasksUseCase
import com.foli.listadetarefas.domain.usecase.task.SetTaskCompletedUseCase
import com.foli.listadetarefas.ui.UiText
import com.foli.listadetarefas.ui.model.TaskUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
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
 * - Combina a observação de tarefas (todas, já ordenadas) com as preferências
 *   persistidas (aba selecionada) e projeta as linhas por aba.
 * - `TabSelected` atualiza o estado imediatamente e persiste em DataStore; a
 *   aba selecionada é restaurada ao reabrir o app.
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
    private val timeProvider: TimeProvider,
    private val timeSignals: TimeSignals,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(TaskListState())
    val state: StateFlow<TaskListState> = _state.asStateFlow()

    private val _effects = Channel<TaskListEffect>(Channel.BUFFERED)
    val effects: Flow<TaskListEffect> = _effects.receiveAsFlow()

    private var observeJob: Job? = null
    private var latestTasks: List<Task> = emptyList()
    private var latestLabels: List<TaskLabel> = emptyList()

    init {
        startObserving()
    }

    fun onAction(action: TaskListAction) {
        // Toda ação recalcula o prazo antes de reduzir: é a "primeira
        // recomposição após a virada" prevista na US-05 AC 5.
        refreshDeadlines()
        when (action) {
            is TaskListAction.TabSelected -> selectTab(action.tab)
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
                Triple(tasks, labels, preferences.selectedTab)
            }
                .catch {
                    slowLoadingJob.cancel()
                    _state.update { current -> current.copy(isLoading = false, loadFailed = true) }
                }
                .collect { (tasks, labels, tab) ->
                    firstEmissionReceived = true
                    slowLoadingJob.cancel()
                    latestTasks = tasks
                    latestLabels = labels
                    _state.update { current ->
                        current.copy(
                            tab = tab,
                            rows = project(tasks, labels, tab, timeProvider.today()),
                            isLoading = false,
                            loadFailed = false,
                        )
                    }
                }
        }
    }

    private fun project(
        tasks: List<Task>,
        labels: List<TaskLabel>,
        tab: TaskTab,
        today: java.time.LocalDate,
    ): List<TaskUiModel> {
        val labelById = labels.associateBy { it.id }
        return TaskFilter.byTab(tasks, tab).map { task ->
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
                rows = project(latestTasks, latestLabels, current.tab, timeProvider.today()),
            )
        }
    }

    private fun selectTab(tab: TaskTab) {
        if (_state.value.tab == tab) return
        _state.update { current ->
            current.copy(
                tab = tab,
                rows = project(latestTasks, latestLabels, tab, timeProvider.today()),
            )
        }
        viewModelScope.launch { setSelectedTabUseCase(tab) }
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
        }
    }

    private fun emitEffect(effect: TaskListEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private companion object {
        const val LOADING_DELAY_MS = 200L
    }
}
