package com.foli.listadetarefas.domain.model

/**
 * Estados possíveis do indicador de proximidade da data limite (US-05 AC 3).
 * Quando a data está a mais de 7 dias não há estado (o classificador devolve
 * `null`), e tarefas sem data ou concluídas também não exibem indicador.
 */
enum class DeadlineState {
    OVERDUE,
    TODAY,
    TOMORROW,
    SOON,
}
