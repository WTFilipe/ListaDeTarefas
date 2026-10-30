package com.foli.listadetarefas.domain.model

/**
 * Limites de texto do produto (ARCHITECTURE.md §3).
 *
 * Nesta história o modelo é **inerte**: não há contadores, bloqueio de digitação
 * nem validação de limite (isso pertence à US-02/US-04).
 */
object TaskLimits {
    const val TITLE_MAX = 120
    const val DESCRIPTION_MAX = 2000
    const val LABEL_NAME_MAX = 30
    const val LINE_MAX = 2
}
