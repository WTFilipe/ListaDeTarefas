package com.foli.listadetarefas.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.foli.listadetarefas.core.di.IoDispatcher
import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.data.local.db.AppDatabase
import com.foli.listadetarefas.data.local.db.LabelDao
import com.foli.listadetarefas.data.local.db.TaskDao
import com.foli.listadetarefas.data.mapper.LabelMapper
import com.foli.listadetarefas.domain.model.TaskLabel
import com.foli.listadetarefas.domain.repository.LabelRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementação Room do [LabelRepository].
 *
 * - Nome duplicado (índice único de `name_key`) vira
 *   [AppFailure.DuplicateLabelName], nunca uma exceção de SQLite na UI.
 * - A exclusão roda em transação: limpa `label_id` das tarefas e depois remove a
 *   etiqueta; a FK `ON DELETE SET NULL` é a segunda linha de defesa.
 */
class LabelRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val labelDao: LabelDao,
    private val taskDao: TaskDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : LabelRepository {

    override fun observeLabels(): Flow<List<TaskLabel>> =
        labelDao.observeAll().map { entities -> entities.map(LabelMapper::toDomain) }

    override suspend fun getLabelById(id: String): AppResult<TaskLabel?> =
        guard { labelDao.getById(id)?.let(LabelMapper::toDomain) }

    override suspend fun getByNormalizedNameKey(nameKey: String): AppResult<TaskLabel?> =
        guard { labelDao.getByNameKey(nameKey)?.let(LabelMapper::toDomain) }

    override suspend fun upsert(label: TaskLabel): AppResult<Unit> =
        withContext(ioDispatcher) {
            try {
                labelDao.upsert(LabelMapper.toEntity(label))
                AppResult.Success(Unit)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (constraint: SQLiteConstraintException) {
                AppResult.Failure(AppFailure.DuplicateLabelName)
            } catch (error: Throwable) {
                AppResult.Failure(AppFailure.StorageUnavailable)
            }
        }

    override suspend fun deleteById(id: String): AppResult<Unit> =
        withContext(ioDispatcher) {
            try {
                database.withTransaction {
                    taskDao.clearLabel(id)
                    labelDao.deleteById(id)
                }
                AppResult.Success(Unit)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                AppResult.Failure(AppFailure.StorageUnavailable)
            }
        }

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
