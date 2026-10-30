package com.foli.listadetarefas.ui.feature.editor.presentation

import com.foli.listadetarefas.ui.UiText

/** Modo do formulário: criação (id gerado uma vez) ou edição de tarefa existente. */
sealed interface EditorMode {
    val taskId: String

    data class Create(override val taskId: String) : EditorMode
    data class Edit(override val taskId: String) : EditorMode
}

/**
 * Estado imutável da S2. Sem contadores/labels/prazo (histórias posteriores).
 * `taskLoadFailed` controla o banner de falha ao carregar em edição; `bannerError`
 * cobre falha ao salvar, sempre preservando o conteúdo digitado.
 */
data class TaskEditorState(
    val mode: EditorMode,
    val title: String = "",
    val description: String = "",
    val titleError: UiText? = null,
    val bannerError: UiText? = null,
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val taskLoadFailed: Boolean = false,
)
