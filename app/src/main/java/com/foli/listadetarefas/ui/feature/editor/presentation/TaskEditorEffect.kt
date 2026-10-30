package com.foli.listadetarefas.ui.feature.editor.presentation

import com.foli.listadetarefas.ui.UiText

/** Campos que podem receber foco por validação. */
enum class EditorField { TITLE, LABEL_NAME }

/** Efeitos one-shot da S2. */
sealed interface TaskEditorEffect {
    data object NavigateBack : TaskEditorEffect
    data class RequestFieldFocus(val field: EditorField) : TaskEditorEffect
    data class ShowStorageError(val message: UiText) : TaskEditorEffect
}
