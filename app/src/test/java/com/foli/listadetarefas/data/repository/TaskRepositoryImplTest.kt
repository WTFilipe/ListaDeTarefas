package com.foli.listadetarefas.data.repository

import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.data.local.db.TaskDao
import com.foli.listadetarefas.data.local.db.TaskEntity
import com.foli.listadetarefas.domain.model.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskRepositoryImplTest {

    private fun repository(dao: TaskDao, dispatcher: kotlinx.coroutines.CoroutineDispatcher) =
        TaskRepositoryImpl(taskDao = dao, ioDispatcher = dispatcher)

    private fun task(id: String, createdAt: Long, title: String = "Título") = Task(
        id = id,
        title = title,
        description = "",
        createdAt = createdAt,
    )

    @Test
    fun upsert_isIdempotentByPrimaryKey() = runTest {
        val dao = FakeTaskDao()
        val repository = repository(dao, StandardTestDispatcher(testScheduler))

        repository.upsert(task("id-1", 100L))
        repository.upsert(task("id-1", 100L))

        val stored = repository.observeTasks().first()
        assertEquals(1, stored.size)
        assertEquals("id-1", stored.single().id)
    }

    @Test
    fun update_keepsCreatedAtAndRewritesContent() = runTest {
        val dao = FakeTaskDao()
        val repository = repository(dao, StandardTestDispatcher(testScheduler))

        repository.upsert(task("id-1", 100L, title = "Antigo"))
        repository.upsert(task("id-1", 100L, title = "Novo"))

        val stored = repository.observeTasks().first().single()
        assertEquals("Novo", stored.title)
        assertEquals(100L, stored.createdAt)
    }

    @Test
    fun deleteById_removesOnlyTheTargetTask() = runTest {
        val dao = FakeTaskDao()
        val repository = repository(dao, StandardTestDispatcher(testScheduler))

        repository.upsert(task("id-1", 100L))
        repository.upsert(task("id-2", 200L))
        val result = repository.deleteById("id-1")

        assertTrue(result is AppResult.Success)
        val stored = repository.observeTasks().first().map { it.id }
        assertEquals(listOf("id-2"), stored)
    }

    @Test
    fun getTaskById_returnsMappedDomainTask() = runTest {
        val dao = FakeTaskDao()
        val repository = repository(dao, StandardTestDispatcher(testScheduler))
        repository.upsert(task("id-1", 100L))

        val result = repository.getTaskById("id-1")

        assertTrue(result is AppResult.Success)
        assertEquals("id-1", (result as AppResult.Success).data?.id)
    }

    @Test
    fun setCompleted_onlyChangesCompletionAndKeepsCreatedAt() = runTest {
        val dao = FakeTaskDao()
        val repository = repository(dao, StandardTestDispatcher(testScheduler))
        repository.upsert(task("id-1", 100L, title = "Preservada"))

        repository.setCompleted("id-1", 999L)

        val stored = repository.observeTasks().first().single()
        assertEquals(999L, stored.completedAt)
        assertEquals(100L, stored.createdAt)
        assertEquals("Preservada", stored.title)
    }

    @Test
    fun setCompleted_withNull_reopensTask() = runTest {
        val dao = FakeTaskDao()
        val repository = repository(dao, StandardTestDispatcher(testScheduler))
        repository.upsert(task("id-1", 100L))
        repository.setCompleted("id-1", 999L)

        repository.setCompleted("id-1", null)

        assertEquals(null, repository.observeTasks().first().single().completedAt)
    }

    @Test
    fun storageFailure_isConvertedToStorageUnavailable() = runTest {
        val dao = FakeTaskDao().apply { failWrites = true }
        val repository = repository(dao, StandardTestDispatcher(testScheduler))

        val upsert = repository.upsert(task("id-1", 100L))
        val delete = repository.deleteById("id-1")

        assertEquals(AppFailure.StorageUnavailable, (upsert as AppResult.Failure).failure)
        assertEquals(AppFailure.StorageUnavailable, (delete as AppResult.Failure).failure)
    }

    private class FakeTaskDao : TaskDao {
        private val rows = MutableStateFlow<List<TaskEntity>>(emptyList())
        var failWrites: Boolean = false

        override fun observeAll(): Flow<List<TaskEntity>> = rows

        override fun observeById(id: String): Flow<TaskEntity?> =
            rows.map { list -> list.firstOrNull { it.id == id } }

        override suspend fun getById(id: String): TaskEntity? {
            if (failWrites) throw IllegalStateException("falha de leitura simulada")
            return rows.value.firstOrNull { it.id == id }
        }

        override suspend fun upsert(entity: TaskEntity) {
            if (failWrites) throw IllegalStateException("falha de escrita simulada")
            rows.update { list -> list.filterNot { it.id == entity.id } + entity }
        }

        override suspend fun deleteById(id: String) {
            if (failWrites) throw IllegalStateException("falha de exclusão simulada")
            rows.update { list -> list.filterNot { it.id == id } }
        }

        override suspend fun setCompleted(id: String, completedAt: Long?) {
            if (failWrites) throw IllegalStateException("falha de conclusão simulada")
            rows.update { list ->
                list.map { entity ->
                    if (entity.id == id) entity.copy(completedAt = completedAt) else entity
                }
            }
        }
    }
}
