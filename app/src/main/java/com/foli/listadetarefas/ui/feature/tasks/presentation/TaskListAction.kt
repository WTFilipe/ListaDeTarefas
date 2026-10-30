package com.foli.listadetarefas.ui.feature.tasks.presentation

import com.foli.listadetarefas.domain.model.TaskTab

/** Intenções da S1. */
sealed interface TaskListAction {
    data class QueryChanged(val value: String) : TaskListAction
    data object SearchCleared : TaskListAction
    data class TabSelected(val tab: TaskTab) : TaskListAction
    data class CompletionToggled(val taskId: String, val isCompleted: Boolean) : TaskListAction
    data class TaskClicked(val taskId: String) : TaskListAction
    data object NewTaskClicked : TaskListAction
    data object LabelsClicked : TaskListAction
    data class DeleteRequested(val taskId: String) : TaskListAction
    data object DeleteConfirmed : TaskListAction
    data object DeleteDismissed : TaskListAction
    data object RetryLoadClicked : TaskListAction
    data object TimeRefreshRequested : TaskListAction
    data class RetryRequested(val operation: TaskRetryOperation) : TaskListAction
}
