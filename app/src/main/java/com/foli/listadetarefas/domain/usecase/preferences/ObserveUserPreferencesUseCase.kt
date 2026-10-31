package com.foli.listadetarefas.domain.usecase.preferences

import com.foli.listadetarefas.domain.model.UserPreferences
import com.foli.listadetarefas.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observa as preferências persistentes da S1 (aba selecionada). */
class ObserveUserPreferencesUseCase @Inject constructor(
    private val repository: PreferencesRepository,
) {
    operator fun invoke(): Flow<UserPreferences> = repository.observePreferences()
}
