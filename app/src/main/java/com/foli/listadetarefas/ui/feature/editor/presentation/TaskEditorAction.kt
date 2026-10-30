package com.foli.listadetarefas.ui.feature.editor.presentation

/** Intenções da S2. */
sealed interface TaskEditorAction {
    data class TitleChanged(val value: String) : TaskEditorAction
    data class DescriptionChanged(val value: String) : TaskEditorAction
    data object SaveClicked : TaskEditorAction
    data object RetrySaveClicked : TaskEditorAction
    data object RetryLoadTaskClicked : TaskEditorAction
}
