package com.foli.listadetarefas.ui.feature.tasks.presentation

/**
 * Operação pendente carregada pelo efeito de erro, permitindo repetir exatamente
 * a mesma ação ("Tentar novamente") sem estado oculto no ViewModel
 * (`ARCHITECTURE.md` §4.1, M3).
 */
sealed interface TaskRetryOperation {
    data class DeleteTask(val taskId: String) : TaskRetryOperation
}
