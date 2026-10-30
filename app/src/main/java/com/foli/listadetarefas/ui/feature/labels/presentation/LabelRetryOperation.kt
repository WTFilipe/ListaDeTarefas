package com.foli.listadetarefas.ui.feature.labels.presentation

/** Operação pendente para repetir exatamente a mesma escrita (ARCHITECTURE.md §4.3). */
sealed interface LabelRetryOperation {
    data object LoadLabels : LabelRetryOperation
    data class SaveLabel(val labelId: String?) : LabelRetryOperation
    data class DeleteLabel(val labelId: String) : LabelRetryOperation
}
