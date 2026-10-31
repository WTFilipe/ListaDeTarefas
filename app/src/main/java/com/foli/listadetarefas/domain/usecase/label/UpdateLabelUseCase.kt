package com.foli.listadetarefas.domain.usecase.label

import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.domain.logic.LabelValidator
import com.foli.listadetarefas.domain.model.TaskLabel
import com.foli.listadetarefas.domain.repository.LabelRepository
import javax.inject.Inject

/**
 * Edita nome e cor de uma etiqueta existente, aplicando as mesmas regras de
 * validação/unicidade da criação. `createdAt` é preservado.
 */
class UpdateLabelUseCase @Inject constructor(
    private val repository: LabelRepository,
) {
    suspend operator fun invoke(
        id: String,
        name: String,
        colorKey: LabelColorKey,
    ): AppResult<TaskLabel> {
        val existing = when (val result = repository.getLabelById(id)) {
            is AppResult.Failure -> return result
            is AppResult.Success -> result.data ?: return AppResult.Failure(AppFailure.NotFound)
        }
        val validated = when (val result = LabelValidator.validateWithColor(name, colorKey)) {
            is AppResult.Failure -> return result
            is AppResult.Success -> result.data
        }
        val duplicate = when (val result = repository.getByNormalizedNameKey(validated.nameKey)) {
            is AppResult.Failure -> return result
            is AppResult.Success -> result.data
        }
        if (duplicate != null && duplicate.id != id) {
            return AppResult.Failure(AppFailure.DuplicateLabelName)
        }
        val updated = existing.copy(
            name = validated.name,
            nameKey = validated.nameKey,
            colorKey = validated.colorKey,
        )
        return when (val write = repository.upsert(updated)) {
            is AppResult.Failure -> write
            is AppResult.Success -> AppResult.Success(updated)
        }
    }
}
