package com.foli.listadetarefas.data.mapper

import com.foli.listadetarefas.data.local.db.TaskEntity
import com.foli.listadetarefas.domain.model.Task
import java.time.LocalDate

/**
 * Conversão domínio <-> entidade. Sem `TypeConverter`: datas viajam como
 * `epochDay`/`epochMillis` e a conversão para `LocalDate` acontece aqui.
 */
object TaskMapper {

    fun toEntity(task: Task): TaskEntity = TaskEntity(
        id = task.id,
        title = task.title,
        description = task.description,
        labelId = task.labelId,
        dueDate = task.dueDate?.toEpochDay(),
        createdAt = task.createdAt,
        completedAt = task.completedAt,
    )

    fun toDomain(entity: TaskEntity): Task = Task(
        id = entity.id,
        title = entity.title,
        description = entity.description,
        labelId = entity.labelId,
        dueDate = entity.dueDate?.let(LocalDate::ofEpochDay),
        createdAt = entity.createdAt,
        completedAt = entity.completedAt,
    )
}
