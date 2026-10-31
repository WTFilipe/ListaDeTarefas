package com.foli.listadetarefas.domain.usecase.label

import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.domain.repository.LabelRepository
import javax.inject.Inject

/**
 * Exclui uma etiqueta. O repositório limpa `label_id` das tarefas na mesma
 * transação, garantindo que elas fiquem sem etiqueta sem perder outros dados.
 */
class DeleteLabelUseCase @Inject constructor(
    private val repository: LabelRepository,
) {
    suspend operator fun invoke(id: String): AppResult<Unit> = repository.deleteById(id)
}
