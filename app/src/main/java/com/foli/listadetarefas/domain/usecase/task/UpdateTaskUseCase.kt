package com.foli.listadetarefas.domain.usecase.task

import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.domain.logic.TaskValidator
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.model.TaskDraft
import com.foli.listadetarefas.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Atualiza uma tarefa existente por id. `createdAt` e `completedAt` são
 * preservados; tarefa inexistente devolve [AppFailure.NotFound].
 */
class UpdateTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(draft: TaskDraft): AppResult<Task> {
        val existing = when (val read = repository.getTaskById(draft.id)) {
            is AppResult.Failure -> return read
            is AppResult.Success -> read.data ?: return AppResult.Failure(AppFailure.NotFound)
        }
        val normalized = when (val result = TaskValidator.validate(draft.title, draft.description)) {
            is AppResult.Failure -> return result
            is AppResult.Success -> result.data
        }
        // A US-04 edita a etiqueta no formulário; o prazo (US-05) ainda não é
        // editável e permanece o valor existente.
        val updated = existing.copy(
            title = normalized.title,
            description = normalized.description,
            labelId = draft.labelId,
        )
        return when (val write = repository.upsert(updated)) {
            is AppResult.Failure -> write
            is AppResult.Success -> AppResult.Success(updated)
        }
    }
}
