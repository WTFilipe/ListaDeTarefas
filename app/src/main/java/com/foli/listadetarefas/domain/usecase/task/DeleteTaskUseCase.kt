package com.foli.listadetarefas.domain.usecase.task

import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.domain.repository.TaskRepository
import javax.inject.Inject

/** Exclui uma tarefa por id, removendo também o registro do armazenamento. */
class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(taskId: String): AppResult<Unit> =
        repository.deleteById(taskId)
}
