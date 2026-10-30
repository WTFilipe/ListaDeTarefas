package com.foli.listadetarefas.ui.feature.tasks.presentation

import com.foli.listadetarefas.ui.UiText

/** Efeitos one-shot da S1. */
sealed interface TaskListEffect {
    data class NavigateToEditor(val taskId: String?) : TaskListEffect
    data class ShowStorageError(
        val message: UiText,
        val operation: TaskRetryOperation,
    ) : TaskListEffect
}
