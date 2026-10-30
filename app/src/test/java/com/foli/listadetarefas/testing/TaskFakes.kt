package com.foli.listadetarefas.testing

import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.core.time.TimeProvider
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.repository.TaskRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate

/**
 * Apoio de teste compartilhado entre as tarefas da US-01 (T02/T04/T05/T06).
 *
 * Não é código de produção: apenas um dublê em memória do contrato de
 * repositório e do provedor de tempo, sem Android, Hilt ou Room.
 */
class FakeTaskRepository : TaskRepository {

    val tasks = MutableStateFlow<List<Task>>(emptyList())

    var upsertCalls: Int = 0
    var deleteCalls: Int = 0
    var failUpsert: Boolean = false
    var failDelete: Boolean = false
    var failRead: Boolean = false
    var readDelayMillis: Long = 0L

    override fun observeTasks(): Flow<List<Task>> =
        if (failRead) {
            flow { throw IllegalStateException("falha de armazenamento simulada") }
        } else {
            flow {
                if (readDelayMillis > 0L) delay(readDelayMillis)
                emitAll(tasks)
            }
        }

    override fun observeTask(id: String): Flow<Task?> =
        if (failRead) {
            flow { throw IllegalStateException("falha de armazenamento simulada") }
        } else {
            tasks.map { list -> list.firstOrNull { it.id == id } }
        }

    override suspend fun getTaskById(id: String): AppResult<Task?> =
        if (failRead) {
            AppResult.Failure(AppFailure.StorageUnavailable)
        } else {
            AppResult.Success(tasks.value.firstOrNull { it.id == id })
        }

    override suspend fun upsert(task: Task): AppResult<Unit> {
        upsertCalls++
        if (failUpsert) return AppResult.Failure(AppFailure.StorageUnavailable)
        tasks.update { list -> list.filterNot { it.id == task.id } + task }
        return AppResult.Success(Unit)
    }

    override suspend fun deleteById(id: String): AppResult<Unit> {
        deleteCalls++
        if (failDelete) return AppResult.Failure(AppFailure.StorageUnavailable)
        tasks.update { list -> list.filterNot { it.id == id } }
        return AppResult.Success(Unit)
    }
}

/** Tempo determinístico: nenhum teste depende do relógio real. */
class FakeTimeProvider(
    var nowMillisValue: Long = 1_000L,
    var todayValue: LocalDate = LocalDate.of(2026, 1, 1),
) : TimeProvider {
    override fun today(): LocalDate = todayValue
    override fun nowMillis(): Long = nowMillisValue
}
