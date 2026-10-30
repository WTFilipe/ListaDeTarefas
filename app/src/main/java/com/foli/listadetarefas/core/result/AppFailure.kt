package com.foli.listadetarefas.core.result

/**
 * Falhas de negócio/infraestrutura conhecidas. A feature traduz cada falha em
 * [com.foli.listadetarefas.ui.UiText]; o domínio nunca conhece strings.
 */
sealed interface AppFailure {
    data object StorageUnavailable : AppFailure
    data object NotFound : AppFailure
    data object DuplicateLabelName : AppFailure
    data object EmptyTitle : AppFailure
    data object EmptyLabelName : AppFailure
    data object TextTooLong : AppFailure
}
