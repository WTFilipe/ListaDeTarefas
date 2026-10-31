package com.foli.listadetarefas.ui.feature.tasks.presentation

import com.foli.listadetarefas.domain.model.SortOrder

/**
 * Operação pendente carregada pelo efeito de erro, permitindo repetir exatamente
 * a mesma ação ("Tentar novamente") sem estado oculto no ViewModel
 * (`ARCHITECTURE.md` §4.1, M3).
 */
sealed interface TaskRetryOperation {
    data class DeleteTask(val taskId: String) : TaskRetryOperation
    data class ToggleCompletion(val taskId: String, val isCompleted: Boolean) : TaskRetryOperation

    /** Regrava a ordenação escolhida depois de uma falha de I/O (US-07 M4). */
    data class SetSortOrder(val sortOrder: SortOrder) : TaskRetryOperation
}
