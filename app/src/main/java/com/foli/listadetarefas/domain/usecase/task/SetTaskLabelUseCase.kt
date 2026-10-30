package com.foli.listadetarefas.domain.usecase.task

import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Associa (ou remove, com `null`) a etiqueta de uma tarefa. Operação idempotente
 * que altera somente `label_id`.
 */
class SetTaskLabelUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(taskId: String, labelId: String?): AppResult<Unit> =
        repository.setLabel(taskId, labelId)
}
