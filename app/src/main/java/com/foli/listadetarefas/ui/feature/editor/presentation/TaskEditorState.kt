package com.foli.listadetarefas.ui.feature.editor.presentation

import com.foli.listadetarefas.ui.UiText
import com.foli.listadetarefas.ui.model.LabelEditorUiModel
import com.foli.listadetarefas.ui.model.LabelUiModel
import java.time.LocalDate

/** Modo do formulário: criação (id gerado uma vez) ou edição de tarefa existente. */
sealed interface EditorMode {
    val taskId: String

    data class Create(override val taskId: String) : EditorMode
    data class Edit(override val taskId: String) : EditorMode
}

/**
 * Estado imutável da S2. `dueDate` é a data limite opcional (somente data) e
 * `today` alimenta o date picker sem consultar o relógio na UI (US-05).
 * `taskLoadFailed` controla o banner de falha ao carregar em edição; `bannerError`
 * cobre falha ao salvar, sempre preservando o conteúdo digitado.
 */
data class TaskEditorState(
    val mode: EditorMode,
    val title: String = "",
    val description: String = "",
    val selectedLabelId: String? = null,
    val availableLabels: List<LabelUiModel> = emptyList(),
    val dueDate: LocalDate? = null,
    val today: LocalDate = LocalDate.of(1970, 1, 1),
    val titleError: UiText? = null,
    val bannerError: UiText? = null,
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val taskLoadFailed: Boolean = false,
    val isLabelLoading: Boolean = false,
    val labelsLoadFailed: Boolean = false,
    val labelEditor: LabelEditorUiModel? = null,
)
