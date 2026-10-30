package com.foli.listadetarefas.ui.feature.editor.presentation

/** Campos que podem receber foco por validação. */
enum class EditorField { TITLE }

/** Efeitos one-shot da S2. */
sealed interface TaskEditorEffect {
    data object NavigateBack : TaskEditorEffect
    data class RequestFieldFocus(val field: EditorField) : TaskEditorEffect
}
