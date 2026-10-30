package com.foli.listadetarefas.ui.feature.editor.presentation

import com.foli.listadetarefas.core.palette.LabelColorKey

/** Intenções da S2 (inclui etiqueta e S4/S7 da US-04). */
sealed interface TaskEditorAction {
    data class TitleChanged(val value: String) : TaskEditorAction
    data class DescriptionChanged(val value: String) : TaskEditorAction
    data class LabelSelected(val labelId: String?) : TaskEditorAction
    data object LabelCreateShortcutClicked : TaskEditorAction
    data class LabelEditorNameChanged(val value: String) : TaskEditorAction
    data class LabelEditorColorSelected(val colorKey: LabelColorKey) : TaskEditorAction
    data object LabelEditorSaveClicked : TaskEditorAction
    data object LabelEditorCancelClicked : TaskEditorAction
    data object SaveClicked : TaskEditorAction
    data object RetrySaveClicked : TaskEditorAction
    data object RetryLoadLabelsClicked : TaskEditorAction
    data object RetryLoadTaskClicked : TaskEditorAction
}
