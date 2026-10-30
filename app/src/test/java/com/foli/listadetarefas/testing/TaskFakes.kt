package com.foli.listadetarefas.testing

import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.core.time.TimeProvider
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.model.TaskLabel
import com.foli.listadetarefas.domain.model.TaskTab
import com.foli.listadetarefas.domain.model.UserPreferences
import com.foli.listadetarefas.domain.repository.LabelRepository
import com.foli.listadetarefas.domain.repository.PreferencesRepository
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
    var setCompletedCalls: Int = 0
    var failUpsert: Boolean = false
    var failDelete: Boolean = false
    var failSetCompleted: Boolean = false
    var failSetLabel: Boolean = false
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

    override suspend fun setCompleted(id: String, completedAt: Long?): AppResult<Unit> {
        setCompletedCalls++
        if (failSetCompleted) return AppResult.Failure(AppFailure.StorageUnavailable)
        tasks.update { list ->
            list.map { task -> if (task.id == id) task.copy(completedAt = completedAt) else task }
        }
        return AppResult.Success(Unit)
    }

    override suspend fun setLabel(id: String, labelId: String?): AppResult<Unit> {
        if (failSetLabel) return AppResult.Failure(AppFailure.StorageUnavailable)
        tasks.update { list ->
            list.map { task -> if (task.id == id) task.copy(labelId = labelId) else task }
        }
        return AppResult.Success(Unit)
    }
}

/** Dublê em memória do contrato de etiquetas (US-04). */
class FakeLabelRepository : LabelRepository {

    val labels = MutableStateFlow<List<TaskLabel>>(emptyList())

    var upsertCalls: Int = 0
    var deleteCalls: Int = 0
    var failUpsert: Boolean = false
    var failDelete: Boolean = false
    var failRead: Boolean = false
    var readDelayMillis: Long = 0L

    override fun observeLabels(): Flow<List<TaskLabel>> =
        if (failRead) {
            flow { throw IllegalStateException("falha de armazenamento simulada") }
        } else {
            flow {
                if (readDelayMillis > 0L) delay(readDelayMillis)
                emitAll(labels)
            }
        }

    override suspend fun getLabelById(id: String): AppResult<TaskLabel?> =
        if (failRead) {
            AppResult.Failure(AppFailure.StorageUnavailable)
        } else {
            AppResult.Success(labels.value.firstOrNull { it.id == id })
        }

    override suspend fun getByNormalizedNameKey(nameKey: String): AppResult<TaskLabel?> =
        if (failRead) {
            AppResult.Failure(AppFailure.StorageUnavailable)
        } else {
            AppResult.Success(labels.value.firstOrNull { it.nameKey == nameKey })
        }

    override suspend fun upsert(label: TaskLabel): AppResult<Unit> {
        upsertCalls++
        if (failUpsert) return AppResult.Failure(AppFailure.StorageUnavailable)
        val duplicate = labels.value.any {
            it.nameKey == label.nameKey && it.id != label.id
        }
        if (duplicate) return AppResult.Failure(AppFailure.DuplicateLabelName)
        labels.update { list -> list.filterNot { it.id == label.id } + label }
        return AppResult.Success(Unit)
    }

    override suspend fun deleteById(id: String): AppResult<Unit> {
        deleteCalls++
        if (failDelete) return AppResult.Failure(AppFailure.StorageUnavailable)
        labels.update { list -> list.filterNot { it.id == id } }
        return AppResult.Success(Unit)
    }
}

/** Tempo determinístico: nenhum teste depende do relógio real. */
class FakePreferencesRepository : PreferencesRepository {

    val preferences = MutableStateFlow(UserPreferences())
    var setSelectedTabCalls: Int = 0

    override fun observePreferences(): Flow<UserPreferences> = preferences

    override suspend fun setSelectedTab(tab: TaskTab) {
        setSelectedTabCalls++
        preferences.update { it.copy(selectedTab = tab) }
    }
}

class FakeTimeProvider(
    var nowMillisValue: Long = 1_000L,
    var todayValue: LocalDate = LocalDate.of(2026, 1, 1),
) : TimeProvider {
    override fun today(): LocalDate = todayValue
    override fun nowMillis(): Long = nowMillisValue
}
