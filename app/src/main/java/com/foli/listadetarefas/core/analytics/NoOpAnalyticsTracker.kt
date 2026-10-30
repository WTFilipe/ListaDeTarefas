package com.foli.listadetarefas.core.analytics

import javax.inject.Inject

/**
 * Implementação no-op usada no MVP: descarta todos os eventos.
 */
class NoOpAnalyticsTracker @Inject constructor() : AnalyticsTracker {
    override fun track(event: String, params: Map<String, Any>) = Unit
}
