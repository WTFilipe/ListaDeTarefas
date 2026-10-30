package com.foli.listadetarefas.ui.feature.labels.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foli.listadetarefas.R
import com.foli.listadetarefas.core.analytics.AnalyticsEvents
import com.foli.listadetarefas.core.analytics.AnalyticsTracker
import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.domain.logic.LabelValidator
import com.foli.listadetarefas.domain.model.TaskLabel
import com.foli.listadetarefas.domain.model.TaskLimits
import com.foli.listadetarefas.domain.usecase.label.CreateLabelUseCase
import com.foli.listadetarefas.domain.usecase.label.DeleteLabelUseCase
import com.foli.listadetarefas.domain.usecase.label.ObserveLabelsUseCase
import com.foli.listadetarefas.domain.usecase.label.UpdateLabelUseCase
import com.foli.listadetarefas.ui.UiText
import com.foli.listadetarefas.ui.model.LabelEditorUiModel
import com.foli.listadetarefas.ui.model.LabelUiModel
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
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel MVI da S3/S4 (um único ViewModel; o S4 é estado, não destino).
 *
 * - Criação usa um UUID gerado uma única vez ao abrir o sheet, então "Tentar
 *   novamente" não duplica a etiqueta.
 * - Duplicata vira erro inline no nome e mantém o sheet aberto com o texto.
 * - A exclusão é confirmada via `pendingDelete` e limpa o vínculo das tarefas no
 *   repositório, sem perder outros dados.
 */
@HiltViewModel
class LabelListViewModel @Inject constructor(
    private val observeLabelsUseCase: ObserveLabelsUseCase,
    private val createLabelUseCase: CreateLabelUseCase,
    private val updateLabelUseCase: UpdateLabelUseCase,
    private val deleteLabelUseCase: DeleteLabelUseCase,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(LabelListState())
    val state: StateFlow<LabelListState> = _state.asStateFlow()

    private val _effects = Channel<LabelListEffect>(Channel.BUFFERED)
    val effects: Flow<LabelListEffect> = _effects.receiveAsFlow()

    private var observeJob: Job? = null
    private var newLabelId: String? = null

    init {
        startObserving()
    }

    fun onAction(action: LabelListAction) {
        when (action) {
            LabelListAction.CreateClicked -> openCreate()
            is LabelListAction.LabelClicked -> openEdit(action.labelId)
            is LabelListAction.EditorNameChanged -> _state.update { current ->
                val editor = current.editor ?: return@update current
                current.copy(
                    editor = editor.copy(
                        name = LabelValidator.limitName(action.value),
                        nameError = null,
                    ),
                )
            }
            is LabelListAction.EditorColorSelected -> _state.update { current ->
                val editor = current.editor ?: return@update current
                current.copy(editor = editor.copy(colorKey = action.colorKey))
            }
            LabelListAction.EditorSaveClicked -> saveEditor()
            LabelListAction.EditorCancelClicked -> closeEditor()
            is LabelListAction.DeleteRequested -> _state.update { current ->
                current.copy(pendingDelete = current.labels.firstOrNull { it.id == action.labelId })
            }
            LabelListAction.DeleteDismissed -> _state.update { it.copy(pendingDelete = null) }
            LabelListAction.DeleteConfirmed -> confirmDelete()
            LabelListAction.RetryLoadClicked -> startObserving()
            is LabelListAction.RetryRequested -> retry(action.operation)
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
            observeLabelsUseCase()
                .catch {
                    slowLoadingJob.cancel()
                    _state.update { current ->
                        current.copy(isLoading = false, loadFailed = true)
                    }
                }
                .collect { labels ->
                    firstEmissionReceived = true
                    slowLoadingJob.cancel()
                    _state.update { current ->
                        current.copy(
                            labels = labels.map(LabelUiMapper::toUiModel),
                            isLoading = false,
                            loadFailed = false,
                        )
                    }
                }
        }
    }

    private fun openCreate() {
        val id = UUID.randomUUID().toString()
        newLabelId = id
        _state.update {
            it.copy(editor = LabelEditorUiModel(colorKey = LabelColorKey.DEFAULT))
        }
        emitEffect(LabelListEffect.RequestNameFocus(null))
    }

    private fun openEdit(labelId: String) {
        val label = _state.value.labels.firstOrNull { it.id == labelId } ?: return
        newLabelId = null
        _state.update {
            it.copy(
                editor = LabelEditorUiModel(
                    labelId = label.id,
                    name = label.name,
                    colorKey = label.colorKey,
                ),
            )
        }
        emitEffect(LabelListEffect.RequestNameFocus(label.id))
    }

    private fun closeEditor() {
        _state.update { it.copy(editor = null, isSaving = false) }
    }

    private fun saveEditor() {
        val current = _state.value
        val editor = current.editor ?: return
        if (current.isSaving) return

        val labelId = editor.labelId ?: newLabelId ?: return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = if (editor.labelId == null) {
                createLabelUseCase(labelId, editor.name, editor.colorKey)
            } else {
                updateLabelUseCase(labelId, editor.name, editor.colorKey)
            }
            when (result) {
                is AppResult.Success -> {
                    val wasCreate = editor.labelId == null
                    if (wasCreate) {
                        analyticsTracker.track(
                            AnalyticsEvents.LABEL_CREATED,
                            mapOf("color" to result.data.colorKey.name),
                        )
                    } else {
                        val previous = _state.value.labels.firstOrNull { it.id == labelId }
                        analyticsTracker.track(
                            AnalyticsEvents.LABEL_UPDATED,
                            mapOf(
                                "changed_name" to (previous?.name != result.data.name),
                                "changed_color" to (previous?.colorKey != result.data.colorKey),
                            ),
                        )
                    }
                    newLabelId = null
                    _state.update { it.copy(editor = null, isSaving = false) }
                }
                is AppResult.Failure -> handleSaveFailure(result.failure, labelId)
            }
        }
    }

    private fun handleSaveFailure(failure: AppFailure, labelId: String) {
        val message = when (failure) {
            AppFailure.EmptyLabelName -> UiText.Res(R.string.label_name_required)
            AppFailure.DuplicateLabelName -> UiText.Res(R.string.label_name_duplicate)
            AppFailure.TextTooLong -> UiText.Res(
                R.string.support_max_chars,
                listOf(TaskLimits.LABEL_NAME_MAX),
            )
            else -> null
        }
        if (message != null) {
            _state.update { current ->
                val editor = current.editor ?: return@update current
                current.copy(
                    editor = editor.copy(nameError = message),
                    isSaving = false,
                )
            }
        } else {
            _state.update { it.copy(isSaving = false) }
            emitEffect(
                LabelListEffect.ShowStorageError(
                    message = UiText.Res(R.string.storage_error_message),
                    operation = LabelRetryOperation.SaveLabel(labelId),
                ),
            )
        }
    }

    private fun confirmDelete() {
        val label = _state.value.pendingDelete ?: return
        _state.update { it.copy(pendingDelete = null) }
        performDelete(label)
    }

    private fun performDelete(label: LabelUiModel) {
        viewModelScope.launch {
            when (deleteLabelUseCase(label.id)) {
                is AppResult.Success -> analyticsTracker.track(AnalyticsEvents.LABEL_DELETED)
                is AppResult.Failure -> emitEffect(
                    LabelListEffect.ShowStorageError(
                        message = UiText.Res(R.string.storage_delete_error_message),
                        operation = LabelRetryOperation.DeleteLabel(label.id),
                    ),
                )
            }
        }
    }

    private fun retry(operation: LabelRetryOperation) {
        when (operation) {
            LabelRetryOperation.LoadLabels -> startObserving()
            is LabelRetryOperation.DeleteLabel -> {
                val label = _state.value.labels.firstOrNull { it.id == operation.labelId }
                if (label != null) performDelete(label)
            }
            is LabelRetryOperation.SaveLabel -> {
                if (_state.value.editor == null) return
                // Mantém o mesmo id da tentativa (criação idempotente).
                newLabelId = operation.labelId
                saveEditor()
            }
        }
    }

    private fun emitEffect(effect: LabelListEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private companion object {
        const val LOADING_DELAY_MS = 200L
    }
}
