package com.foli.listadetarefas.core.time

import java.time.LocalDate

/**
 * Fonte única de tempo do app. Toda leitura de data/hora passa por aqui para
 * permitir testes determinísticos.
 */
interface TimeProvider {
    fun today(): LocalDate
    fun nowMillis(): Long
}
