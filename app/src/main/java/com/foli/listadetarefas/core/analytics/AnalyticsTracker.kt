package com.foli.listadetarefas.core.analytics

/**
 * Contrato local de analytics. Nenhum SDK remoto e nenhum dado sai do aparelho.
 */
interface AnalyticsTracker {
    fun track(event: String, params: Map<String, Any> = emptyMap())
}
