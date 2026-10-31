package com.foli.listadetarefas.ui.feature.editor.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foli.listadetarefas.R
import com.foli.listadetarefas.core.analytics.AnalyticsEvents
import com.foli.listadetarefas.core.analytics.AnalyticsTracker
import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.core.time.TimeProvider
import com.foli.listadetarefas.domain.logic.LabelValidator
import com.foli.listadetarefas.domain.logic.TaskValidator
import com.foli.listadetarefas.domain.model.TaskDraft
import com.foli.listadetarefas.domain.model.TaskLabel
import com.foli.listadetarefas.domain.model.TaskLimits
import com.foli.listadetarefas.domain.usecase.label.CreateLabelUseCase
import com.foli.listadetarefas.domain.usecase.label.ObserveLabelsUseCase
import com.foli.listadetarefas.domain.usecase.task.CreateTaskUseCase
import com.foli.listadetarefas.domain.usecase.task.ObserveTaskUseCase
import com.foli.listadetarefas.domain.usecase.task.UpdateTaskUseCase
import com.foli.listadetarefas.ui.UiText
import com.foli.listadetarefas.ui.model.LabelEditorUiModel
import com.foli.listadetarefas.ui.model.LabelUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
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
 * ViewModel MVI da S2.
 *
 * - Criação: o UUID é gerado **uma única vez** ao abrir o formulário e reutilizado
 *   em todo `SaveClicked`/`RetrySaveClicked`, então repetir não duplica tarefa.
 * - Escrita só acontece em `SaveClicked`/`RetrySaveClicked`; digitar não grava.
 * - Edição: carrega a tarefa, salva por id sem alterar `createdAt`; se ela não
 *   existir mais, volta para a lista. Falha de leitura vira `taskLoadFailed`.
 * - Limites: `TitleChanged`/`DescriptionChanged` truncam a entrada em 120/2000
 *   (digitação e colagem) e o conteúdo carregado na edição também é normalizado,
 *   de modo que o contador nunca reflete um valor acima do máximo.
 * - Trim/normalização ficam no domínio (`TaskValidator`/use cases), que também
 *   rejeita título vazio e revalida os limites.
 */
@HiltViewModel
class TaskEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val observeTaskUseCase: ObserveTaskUseCase,
    private val observeLabelsUseCase: ObserveLabelsUseCase,
    private val createLabelUseCase: CreateLabelUseCase,
    private val timeProvider: TimeProvider,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val mode: EditorMode = savedStateHandle.get<String>(NAV_ARG_TASK_ID)
        ?.let(EditorMode::Edit)
        ?: EditorMode.Create(UUID.randomUUID().toString())

    private val _state = MutableStateFlow(
        TaskEditorState(mode = mode, today = timeProvider.today()),
    )
    val state: StateFlow<TaskEditorState> = _state.asStateFlow()

    private val _effects = Channel<TaskEditorEffect>(Channel.BUFFERED)
    val effects: Flow<TaskEditorEffect> = _effects.receiveAsFlow()

    private var loadJob: Job? = null
    private var labelsJob: Job? = null
    private var taskLoaded: Boolean = false
    private var newLabelId: String? = null

    /** `true` quando o usuário já alterou o formulário; impede que uma emissão
     *  tardia do carregamento sobrescreva o conteúdo digitado. */
    private var isDirty: Boolean = false

    init {
        startObservingLabels()
        if (mode is EditorMode.Edit) {
            loadTask()
        }
    }

    fun onAction(action: TaskEditorAction) {
        when (action) {
            is TaskEditorAction.TitleChanged -> {
                isDirty = true
                _state.update {
                    it.copy(
                        title = TaskValidator.limitTitle(action.value),
                        titleError = null,
                    )
                }
            }
            is TaskEditorAction.DescriptionChanged -> {
                isDirty = true
                _state.update {
                    it.copy(description = TaskValidator.limitDescription(action.value))
                }
            }
            is TaskEditorAction.LabelSelected -> {
                isDirty = true
                _state.update { it.copy(selectedLabelId = action.labelId) }
            }
            is TaskEditorAction.DueDateSelected -> {
                isDirty = true
                _state.update { it.copy(dueDate = action.date) }
            }
            TaskEditorAction.DueDateRemoved -> {
                isDirty = true
                _state.update { it.copy(dueDate = null) }
            }
            TaskEditorAction.TimeRefreshRequested ->
                _state.update { it.copy(today = timeProvider.today()) }
            TaskEditorAction.LabelCreateShortcutClicked -> {
                newLabelId = UUID.randomUUID().toString()
                _state.update {
                    it.copy(labelEditor = LabelEditorUiModel(colorKey = LabelColorKey.DEFAULT))
                }
            }
            is TaskEditorAction.LabelEditorNameChanged -> _state.update { current ->
                val editor = current.labelEditor ?: return@update current
                current.copy(
                    labelEditor = editor.copy(
                        name = LabelValidator.limitName(action.value),
                        nameError = null,
                    ),
                )
            }
            is TaskEditorAction.LabelEditorColorSelected -> _state.update { current ->
                val editor = current.labelEditor ?: return@update current
                current.copy(labelEditor = editor.copy(colorKey = action.colorKey))
            }
            TaskEditorAction.LabelEditorSaveClicked -> saveLabelFromShortcut()
            TaskEditorAction.LabelEditorCancelClicked ->
                _state.update { it.copy(labelEditor = null) }
            TaskEditorAction.SaveClicked -> save()
            TaskEditorAction.RetrySaveClicked -> save()
            TaskEditorAction.RetryLoadLabelsClicked -> startObservingLabels()
            TaskEditorAction.RetryLoadTaskClicked -> loadTask()
        }
    }

    private fun save() {
        val current = _state.value
        // Não grava enquanto o carregamento está em andamento ou falhou: evita
        // sobrescrever a tarefa existente com dados que o usuário ainda não viu.
        if (current.isSaving || current.isLoading || current.taskLoadFailed) return

        if (TaskValidator.normalizeTitle(current.title).isEmpty()) {
            _state.update { it.copy(titleError = UiText.Res(R.string.task_title_required)) }
            emitEffect(TaskEditorEffect.RequestFieldFocus(EditorField.TITLE))
            return
        }

        _state.update {
            it.copy(titleError = null, bannerError = null, isSaving = true)
        }
        viewModelScope.launch {
            val draft = TaskDraft(
                id = current.mode.taskId,
                title = current.title,
                description = current.description,
                labelId = current.selectedLabelId,
                dueDate = current.dueDate,
            )
            val result = when (current.mode) {
                is EditorMode.Create -> createTaskUseCase(draft)
                is EditorMode.Edit -> updateTaskUseCase(draft)
            }
            when (result) {
                is AppResult.Success -> {
                    trackSuccess(current.mode, draft)
                    _effects.send(TaskEditorEffect.NavigateBack)
                }
                is AppResult.Failure -> when (result.failure) {
                    AppFailure.NotFound -> _effects.send(TaskEditorEffect.NavigateBack)
                    else -> _state.update {
                        it.copy(
                            isSaving = false,
                            bannerError = UiText.Res(R.string.storage_error_inline),
                        )
                    }
                }
            }
        }
    }

    private fun loadTask() {
        val currentMode = _state.value.mode
        if (currentMode !is EditorMode.Edit) return

        taskLoaded = false
        loadJob?.cancel()
        _state.update { it.copy(isLoading = true, taskLoadFailed = false) }
        loadJob = viewModelScope.launch {
            observeTaskUseCase(currentMode.taskId)
                .catch {
                    _state.update { current ->
                        current.copy(isLoading = false, taskLoadFailed = true)
                    }
                }
                .collect { task ->
                    if (task == null) {
                        _effects.send(TaskEditorEffect.NavigateBack)
                    } else if (!taskLoaded) {
                        taskLoaded = true
                        _state.update { current ->
                            if (isDirty) {
                                // O usuário já digitou: mantém o conteúdo e apenas
                                // encerra o carregamento, sem sobrescrever nada.
                                current.copy(isLoading = false, taskLoadFailed = false)
                            } else {
                                current.copy(
                                    title = TaskValidator.limitTitle(task.title),
                                    description = TaskValidator.limitDescription(task.description),
                                    selectedLabelId = task.labelId,
                                    dueDate = task.dueDate,
                                    isLoading = false,
                                    taskLoadFailed = false,
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun startObservingLabels() {
        labelsJob?.cancel()
        _state.update { it.copy(labelsLoadFailed = false, isLabelLoading = false) }
        labelsJob = viewModelScope.launch {
            var firstEmissionReceived = false
            val slowLoadingJob = launch {
                kotlinx.coroutines.delay(LABEL_LOADING_DELAY_MS)
                if (!firstEmissionReceived) {
                    _state.update { it.copy(isLabelLoading = true) }
                }
            }
            observeLabelsUseCase()
                .catch {
                    slowLoadingJob.cancel()
                    _state.update { current ->
                        current.copy(isLabelLoading = false, labelsLoadFailed = true)
                    }
                }
                .collect { labels ->
                    firstEmissionReceived = true
                    slowLoadingJob.cancel()
                    _state.update { current ->
                        current.copy(
                            availableLabels = labels.map(::toLabelUiModel),
                            isLabelLoading = false,
                            labelsLoadFailed = false,
                        )
                    }
                }
        }
    }

    /**
     * Cria a etiqueta pelo atalho do S7 (mesmo use case da S3). Em sucesso fecha
     * o S4 e seleciona a etiqueta nova sem alterar título/descrição; duplicata
     * mantém o sheet aberto com erro inline.
     */
    private fun saveLabelFromShortcut() {
        val editor = _state.value.labelEditor ?: return
        val labelId = newLabelId ?: return
        viewModelScope.launch {
            when (val result = createLabelUseCase(labelId, editor.name, editor.colorKey)) {
                is AppResult.Success -> {
                    analyticsTracker.track(
                        AnalyticsEvents.LABEL_CREATED,
                        mapOf("color" to result.data.colorKey.name),
                    )
                    newLabelId = null
                    _state.update {
                        it.copy(labelEditor = null, selectedLabelId = result.data.id)
                    }
                }
                is AppResult.Failure -> handleLabelEditorFailure(result.failure)
            }
        }
    }

    private fun handleLabelEditorFailure(failure: AppFailure) {
        val inlineError = when (failure) {
            AppFailure.EmptyLabelName -> UiText.Res(R.string.label_name_required)
            AppFailure.DuplicateLabelName -> UiText.Res(R.string.label_name_duplicate)
            AppFailure.TextTooLong -> UiText.Res(
                R.string.support_max_chars,
                listOf(TaskLimits.LABEL_NAME_MAX),
            )
            else -> null
        }
        if (inlineError != null) {
            _state.update { current ->
                val editor = current.labelEditor ?: return@update current
                current.copy(labelEditor = editor.copy(nameError = inlineError))
            }
        } else {
            emitEffect(TaskEditorEffect.ShowStorageError(UiText.Res(R.string.storage_error_message)))
        }
    }

    private fun toLabelUiModel(label: TaskLabel): LabelUiModel = LabelUiModel(
        id = label.id,
        name = label.name,
        colorKey = label.colorKey,
    )

    private fun trackSuccess(mode: EditorMode, draft: TaskDraft) {
        when (mode) {
            is EditorMode.Create -> analyticsTracker.track(
                AnalyticsEvents.TASK_CREATED_SUCCESS,
                mapOf(
                    "has_description" to draft.description.isNotBlank(),
                    "has_label" to (draft.labelId != null),
                    "has_deadline" to (draft.dueDate != null),
                ),
            )
            is EditorMode.Edit -> analyticsTracker.track(
                AnalyticsEvents.TASK_UPDATED,
            )
        }
    }

    private fun emitEffect(effect: TaskEditorEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private companion object {
        const val NAV_ARG_TASK_ID = "taskId"
        const val LABEL_LOADING_DELAY_MS = 200L
    }
}
