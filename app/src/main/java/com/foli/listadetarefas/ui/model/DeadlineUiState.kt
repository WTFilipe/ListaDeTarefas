package com.foli.listadetarefas.ui.model

/**
 * Estado do indicador de prazo no vocabulário de apresentação (US-05 AC 3).
 * `null` em [TaskUiModel.deadline] significa "sem indicador" (sem data,
 * concluída ou a mais de 7 dias).
 */
enum class DeadlineUiState {
    OVERDUE,
    TODAY,
    TOMORROW,
    SOON,
}
