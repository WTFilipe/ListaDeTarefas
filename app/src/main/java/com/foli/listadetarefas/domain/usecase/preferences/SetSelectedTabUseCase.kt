package com.foli.listadetarefas.domain.usecase.preferences

import com.foli.listadetarefas.domain.model.TaskTab
import com.foli.listadetarefas.domain.repository.PreferencesRepository
import javax.inject.Inject

/** Persiste a aba selecionada em S1 (US-03 AC 5). */
class SetSelectedTabUseCase @Inject constructor(
    private val repository: PreferencesRepository,
) {
    suspend operator fun invoke(tab: TaskTab) = repository.setSelectedTab(tab)
}
