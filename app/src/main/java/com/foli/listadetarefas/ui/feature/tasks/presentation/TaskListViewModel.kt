package com.foli.listadetarefas.ui.feature.tasks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foli.listadetarefas.R
import com.foli.listadetarefas.core.analytics.AnalyticsEvents
import com.foli.listadetarefas.core.analytics.AnalyticsTracker
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.domain.usecase.task.DeleteTaskUseCase
import com.foli.listadetarefas.domain.usecase.task.ObserveTasksUseCase
import com.foli.listadetarefas.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel MVI da S1. Observa a lista ativa já ordenada pela ordenação vigente
 * (`ObserveTasksUseCase`), expõe estado imutável e efeitos one-shot. O loading só
 * aparece se a primeira emissão demorar mais de 200 ms (DESIGN_SPEC §8.2).
 */
@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val observeTasksUseCase: ObserveTasksUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(TaskListState())
    val state: StateFlow<TaskListState> = _state.asStateFlow()

    private val _effects = Channel<TaskListEffect>(Channel.BUFFERED)
    val effects: Flow<TaskListEffect> = _effects.receiveAsFlow()

    private var observeJob: Job? = null

    init {
        startObserving()
    }

    fun onAction(action: TaskListAction) {
        when (action) {
            is TaskListAction.TaskClicked -> emitEffect(TaskListEffect.NavigateToEditor(action.taskId))
            TaskListAction.NewTaskClicked -> emitEffect(TaskListEffect.NavigateToEditor(null))
            is TaskListAction.DeleteRequested -> _state.update { it.copy(pendingDeleteId = action.taskId) }
            TaskListAction.DeleteDismissed -> _state.update { it.copy(pendingDeleteId = null) }
            TaskListAction.DeleteConfirmed -> confirmDelete()
            TaskListAction.RetryLoadClicked -> startObserving()
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
            observeTasksUseCase()
                .catch {
                    slowLoadingJob.cancel()
                    _state.update { current -> current.copy(isLoading = false, loadFailed = true) }
                }
                .collect { tasks ->
                    firstEmissionReceived = true
                    slowLoadingJob.cancel()
                    _state.update { current ->
                        current.copy(
                            rows = tasks.map(TaskUiMapper::toUiModel),
                            isLoading = false,
                            loadFailed = false,
                        )
                    }
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
        }
    }

    private fun emitEffect(effect: TaskListEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private companion object {
        const val LOADING_DELAY_MS = 200L
    }
}
