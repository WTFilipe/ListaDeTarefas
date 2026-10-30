package com.foli.listadetarefas.domain.usecase.task

import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.core.time.TimeProvider
import com.foli.listadetarefas.domain.logic.TaskValidator
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.model.TaskDraft
import com.foli.listadetarefas.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Cria uma tarefa a partir do rascunho. O `id` recebido já foi gerado uma única
 * vez pelo formulário e o upsert é idempotente; `createdAt` vem do [TimeProvider].
 */
class CreateTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(draft: TaskDraft): AppResult<Task> {
        val normalized = when (val result = TaskValidator.validate(draft.title, draft.description)) {
            is AppResult.Failure -> return result
            is AppResult.Success -> result.data
        }
        val task = Task(
            id = draft.id,
            title = normalized.title,
            description = normalized.description,
            labelId = draft.labelId,
            dueDate = draft.dueDate,
            createdAt = timeProvider.nowMillis(),
        )
        return when (val write = repository.upsert(task)) {
            is AppResult.Failure -> write
            is AppResult.Success -> AppResult.Success(task)
        }
    }
}
