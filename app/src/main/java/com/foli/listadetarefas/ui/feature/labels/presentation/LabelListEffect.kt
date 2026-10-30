package com.foli.listadetarefas.ui.feature.labels.presentation

import com.foli.listadetarefas.ui.UiText

/** Efeitos one-shot da S3/S4. */
sealed interface LabelListEffect {
    data object NavigateBack : LabelListEffect
    data class RequestNameFocus(val editorId: String?) : LabelListEffect
    data class ShowStorageError(
        val message: UiText,
        val operation: LabelRetryOperation,
    ) : LabelListEffect
}
