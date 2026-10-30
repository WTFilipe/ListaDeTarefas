package com.foli.listadetarefas.ui.feature.labels.presentation

import com.foli.listadetarefas.core.palette.LabelColorKey

/** Intenções da S3/S4 de etiquetas. */
sealed interface LabelListAction {
    data object CreateClicked : LabelListAction
    data class LabelClicked(val labelId: String) : LabelListAction
    data class EditorNameChanged(val value: String) : LabelListAction
    data class EditorColorSelected(val colorKey: LabelColorKey) : LabelListAction
    data object EditorSaveClicked : LabelListAction
    data object EditorCancelClicked : LabelListAction
    data class DeleteRequested(val labelId: String) : LabelListAction
    data object DeleteConfirmed : LabelListAction
    data object DeleteDismissed : LabelListAction
    data object RetryLoadClicked : LabelListAction
    data class RetryRequested(val operation: LabelRetryOperation) : LabelListAction
}
