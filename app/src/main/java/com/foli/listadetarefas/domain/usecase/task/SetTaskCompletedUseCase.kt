package com.foli.listadetarefas.domain.usecase.task

import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.core.time.TimeProvider
import com.foli.listadetarefas.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Conclui ou reabre uma tarefa (US-03 AC 2/4/6). A operação só altera
 * `completedAt` — título, descrição, etiqueta, prazo e `createdAt` são
 * preservados pelo `setCompleted` idempotente do repositório.
 */
class SetTaskCompletedUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(taskId: String, isCompleted: Boolean): AppResult<Unit> =
        repository.setCompleted(
            id = taskId,
            completedAt = if (isCompleted) timeProvider.nowMillis() else null,
        )
}
