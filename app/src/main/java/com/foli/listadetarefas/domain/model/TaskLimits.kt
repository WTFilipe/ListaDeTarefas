package com.foli.listadetarefas.domain.model

/**
 * Limites de texto do produto (ARCHITECTURE.md §3).
 *
 * O título é limitado a [TITLE_MAX] e a descrição a [DESCRIPTION_MAX]; a UI
 * bloqueia o excedente e o domínio revalida ([com.foli.listadetarefas.domain.logic.TaskValidator]).
 * O limite de 2 linhas ([LINE_MAX]) é visual, aplicado na linha da lista.
 */
object TaskLimits {
    const val TITLE_MAX = 120
    const val DESCRIPTION_MAX = 2000
    const val LABEL_NAME_MAX = 30
    const val LINE_MAX = 2
}
