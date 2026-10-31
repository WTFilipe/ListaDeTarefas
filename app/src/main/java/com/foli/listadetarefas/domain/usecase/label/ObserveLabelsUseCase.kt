package com.foli.listadetarefas.domain.usecase.label

import com.foli.listadetarefas.domain.model.TaskLabel
import com.foli.listadetarefas.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observa todas as etiquetas para atualização reativa da S3, S4 e do chip. */
class ObserveLabelsUseCase @Inject constructor(
    private val repository: LabelRepository,
) {
    operator fun invoke(): Flow<List<TaskLabel>> = repository.observeLabels()
}
