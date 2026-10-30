package com.foli.listadetarefas.domain.model

/**
 * Aba visível em S1 (`ARCHITECTURE.md` §4.1). `ACTIVE` é o padrão e a seleção
 * persiste via `PreferencesRepository` (DataStore).
 */
enum class TaskTab {
    ACTIVE,
    COMPLETED,
}
