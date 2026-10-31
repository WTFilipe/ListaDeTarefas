package com.foli.listadetarefas.domain.usecase.preferences

import com.foli.listadetarefas.domain.model.SortOrder
import com.foli.listadetarefas.domain.repository.PreferencesRepository
import javax.inject.Inject

/** Persiste a ordenação escolhida em S1 (US-07 AC 4). */
class SetSortOrderUseCase @Inject constructor(
    private val repository: PreferencesRepository,
) {
    suspend operator fun invoke(order: SortOrder) = repository.setSortOrder(order)
}
