package com.foli.listadetarefas.ui.feature.tasks.presentation

import com.foli.listadetarefas.core.format.DateLabelFormatter
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.ui.model.TaskUiModel

/**
 * Mapper domínio -> `ui/model` da S1. O design system só conhece [TaskUiModel].
 * A data de conclusão é formatada aqui (`dd/MM/aaaa`, data local, sem hora).
 */
object TaskUiMapper {
    fun toUiModel(
        task: Task,
        formatCompletedOn: (Long) -> String = DateLabelFormatter::format,
    ): TaskUiModel = TaskUiModel(
        id = task.id,
        title = task.title,
        description = task.description,
        isCompleted = task.completedAt != null,
        completedOnDate = task.completedAt?.let(formatCompletedOn),
    )
}
