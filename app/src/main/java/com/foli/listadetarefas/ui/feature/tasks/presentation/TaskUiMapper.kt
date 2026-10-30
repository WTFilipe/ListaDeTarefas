package com.foli.listadetarefas.ui.feature.tasks.presentation

import com.foli.listadetarefas.core.format.DateLabelFormatter
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.model.TaskLabel
import com.foli.listadetarefas.ui.model.LabelUiModel
import com.foli.listadetarefas.ui.model.TaskUiModel

/**
 * Mapper domínio -> `ui/model` da S1. O design system só conhece
 * [TaskUiModel]; a etiqueta é convertida para [LabelUiModel] aqui. A data de
 * conclusão é formatada como `dd/MM/aaaa` (data local, sem hora).
 */
object TaskUiMapper {
    fun toUiModel(
        task: Task,
        label: TaskLabel? = null,
        formatCompletedOn: (Long) -> String = DateLabelFormatter::format,
    ): TaskUiModel = TaskUiModel(
        id = task.id,
        title = task.title,
        description = task.description,
        label = label?.let { source ->
            LabelUiModel(
                id = source.id,
                name = source.name,
                colorKey = source.colorKey,
            )
        },
        isCompleted = task.completedAt != null,
        completedOnDate = task.completedAt?.let(formatCompletedOn),
    )
}
