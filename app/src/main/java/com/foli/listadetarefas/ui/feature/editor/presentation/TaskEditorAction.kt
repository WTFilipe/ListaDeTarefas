package com.foli.listadetarefas.ui.feature.editor.presentation

import com.foli.listadetarefas.core.palette.LabelColorKey
import java.time.LocalDate

/** Intenções da S2 (inclui etiqueta/S4/S7 e data limite/S8). */
sealed interface TaskEditorAction {
    data class TitleChanged(val value: String) : TaskEditorAction
    data class DescriptionChanged(val value: String) : TaskEditorAction
    data class LabelSelected(val labelId: String?) : TaskEditorAction
    data class DueDateSelected(val date: LocalDate) : TaskEditorAction
    data object DueDateRemoved : TaskEditorAction
    data object TimeRefreshRequested : TaskEditorAction
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
