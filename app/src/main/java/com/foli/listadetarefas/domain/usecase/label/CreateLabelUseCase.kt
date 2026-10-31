package com.foli.listadetarefas.domain.usecase.label

import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.core.time.TimeProvider
import com.foli.listadetarefas.domain.logic.LabelValidator
import com.foli.listadetarefas.domain.model.TaskLabel
import com.foli.listadetarefas.domain.repository.LabelRepository
import javax.inject.Inject

/**
 * Cria uma etiqueta com id gerado uma única vez pelo chamador. Valida nome/limite,
 * bloqueia duplicatas pela chave normalizada e grava de forma idempotente. A
 * corrida é desempatada pelo índice único de `name_key` no repositório.
 */
class CreateLabelUseCase @Inject constructor(
    private val repository: LabelRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(
        id: String,
        name: String,
        colorKey: LabelColorKey,
    ): AppResult<TaskLabel> {
        val validated = when (val result = LabelValidator.validateWithColor(name, colorKey)) {
            is AppResult.Failure -> return result
            is AppResult.Success -> result.data
        }
        val existing = when (val result = repository.getByNormalizedNameKey(validated.nameKey)) {
            is AppResult.Failure -> return result
            is AppResult.Success -> result.data
        }
        if (existing != null) {
            return AppResult.Failure(AppFailure.DuplicateLabelName)
        }
        val label = TaskLabel(
            id = id,
            name = validated.name,
            nameKey = validated.nameKey,
            colorKey = validated.colorKey,
            createdAt = timeProvider.nowMillis(),
        )
        return when (val write = repository.upsert(label)) {
            is AppResult.Failure -> write
            is AppResult.Success -> AppResult.Success(label)
        }
    }
}
