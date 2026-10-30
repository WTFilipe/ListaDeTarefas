package com.foli.listadetarefas.ui.feature.tasks.presentation

/** Intenções da S1. */
sealed interface TaskListAction {
    data class TaskClicked(val taskId: String) : TaskListAction
    data object NewTaskClicked : TaskListAction
    data class DeleteRequested(val taskId: String) : TaskListAction
    data object DeleteConfirmed : TaskListAction
    data object DeleteDismissed : TaskListAction
    data object RetryLoadClicked : TaskListAction
    data class RetryRequested(val operation: TaskRetryOperation) : TaskListAction
}
