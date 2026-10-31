package com.foli.listadetarefas.ui.feature.tasks.presentation

import com.foli.listadetarefas.core.format.DateLabelFormatter
import com.foli.listadetarefas.domain.logic.DeadlineClassifier
import com.foli.listadetarefas.domain.model.DeadlineState
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.model.TaskLabel
import com.foli.listadetarefas.ui.model.DeadlineUiState
import com.foli.listadetarefas.ui.model.LabelUiModel
import com.foli.listadetarefas.ui.model.TaskUiModel
import java.time.LocalDate

/**
 * Mapper domínio -> `ui/model` da S1. O design system só conhece
 * [TaskUiModel]; a etiqueta é convertida para [LabelUiModel] aqui. A data de
 * conclusão e a data limite são formatadas como `dd/MM/aaaa` (data local, sem
 * hora). O prazo é classificado pela `today` injetada (nunca pelo relógio).
 */
object TaskUiMapper {
    fun toUiModel(
        task: Task,
        label: TaskLabel? = null,
        formatCompletedOn: (Long) -> String = DateLabelFormatter::format,
        today: LocalDate? = null,
        formatDate: (LocalDate) -> String = DateLabelFormatter::formatDate,
    ): TaskUiModel {
        val deadlineState = if (today != null && task.completedAt == null) {
            DeadlineClassifier.classify(today, task.dueDate)
        } else {
            null
        }
        return TaskUiModel(
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
            deadline = deadlineState?.toUiState(),
            deadlineDateLabel = if (deadlineState != null) {
                task.dueDate?.let(formatDate)
            } else {
                null
            },
        )
    }

    private fun DeadlineState.toUiState(): DeadlineUiState = when (this) {
        DeadlineState.OVERDUE -> DeadlineUiState.OVERDUE
        DeadlineState.TODAY -> DeadlineUiState.TODAY
        DeadlineState.TOMORROW -> DeadlineUiState.TOMORROW
        DeadlineState.SOON -> DeadlineUiState.SOON
    }
}
