package com.foli.listadetarefas.data.mapper

import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.data.local.db.LabelEntity
import com.foli.listadetarefas.domain.model.TaskLabel

/**
 * Conversão domínio <-> entidade. Chaves de cor desconhecidas caem em
 * [LabelColorKey.RED] para não quebrar com dados antigos (ARCHITECTURE.md §9.1).
 */
object LabelMapper {

    fun toEntity(label: TaskLabel): LabelEntity = LabelEntity(
        id = label.id,
        name = label.name,
        nameKey = label.nameKey,
        colorKey = label.colorKey.name,
        createdAt = label.createdAt,
    )

    fun toDomain(entity: LabelEntity): TaskLabel = TaskLabel(
        id = entity.id,
        name = entity.name,
        nameKey = entity.nameKey,
        colorKey = LabelColorKey.fromStorageOrNull(entity.colorKey) ?: LabelColorKey.DEFAULT,
        createdAt = entity.createdAt,
    )
}
