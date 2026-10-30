package com.foli.listadetarefas.data.repository

import com.foli.listadetarefas.core.di.IoDispatcher
import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.data.local.db.TaskDao
import com.foli.listadetarefas.data.mapper.TaskMapper
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.repository.TaskRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementação Room do [TaskRepository].
 *
 * - Leituras são reativas (`Flow`) e mapeadas para o domínio.
 * - Escritas rodam em [IoDispatcher] e convertem qualquer falha de
 *   infraestrutura em [AppFailure.StorageUnavailable]; nenhuma exceção de SQLite
 *   chega ao domínio/UI.
 * - `upsert` é idempotente por PK, então repetir a operação ("Tentar novamente")
 *   não duplica a tarefa.
 */
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> =
        taskDao.observeAll().map { entities -> entities.map(TaskMapper::toDomain) }

    override fun observeTask(id: String): Flow<Task?> =
        taskDao.observeById(id).map { entity -> entity?.let(TaskMapper::toDomain) }

    override suspend fun getTaskById(id: String): AppResult<Task?> =
        guard { taskDao.getById(id)?.let(TaskMapper::toDomain) }

    override suspend fun upsert(task: Task): AppResult<Unit> =
        guard { taskDao.upsert(TaskMapper.toEntity(task)) }

    override suspend fun deleteById(id: String): AppResult<Unit> =
        guard { taskDao.deleteById(id) }

    private suspend fun <T> guard(block: suspend () -> T): AppResult<T> =
        withContext(ioDispatcher) {
            try {
                AppResult.Success(block())
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                AppResult.Failure(AppFailure.StorageUnavailable)
            }
        }
}
