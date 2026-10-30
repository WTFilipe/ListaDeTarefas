package com.foli.listadetarefas.ui.feature.tasks.presentation

import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.ui.model.TaskUiModel

/**
 * Mapper domínio -> `ui/model` da S1. O design system só conhece [TaskUiModel].
 */
object TaskUiMapper {
    fun toUiModel(task: Task): TaskUiModel = TaskUiModel(
        id = task.id,
        title = task.title,
        description = task.description,
    )
}
