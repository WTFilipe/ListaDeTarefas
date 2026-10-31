package com.foli.listadetarefas.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Destinos type-safe do app (`ARCHITECTURE.md` §6). Modais (dialogs/sheets) não
 * são destinos: vivem como estado de UI.
 */
@Serializable
sealed interface AppDestination {

    @Serializable
    data object TaskList : AppDestination

    @Serializable
    data class TaskEditor(val taskId: String? = null) : AppDestination

    @Serializable
    data object Labels : AppDestination
}
