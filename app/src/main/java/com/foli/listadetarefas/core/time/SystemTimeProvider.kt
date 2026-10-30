package com.foli.listadetarefas.core.time

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação de produção. Este é o único arquivo do código de produção
 * autorizado a consultar o relógio do sistema (regra verificada por
 * `ArchitectureRulesTest`).
 */
@Singleton
class SystemTimeProvider @Inject constructor() : TimeProvider {
    override fun today(): LocalDate = LocalDate.now()

    override fun nowMillis(): Long = System.currentTimeMillis()
}
