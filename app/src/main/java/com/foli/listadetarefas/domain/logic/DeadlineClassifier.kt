package com.foli.listadetarefas.domain.logic

import com.foli.listadetarefas.domain.model.DeadlineState
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Classificação pura do prazo pela data local (US-05 AC 3).
 *
 * - data anterior a hoje → [DeadlineState.OVERDUE]
 * - data igual a hoje → [DeadlineState.TODAY]
 * - hoje + 1 dia → [DeadlineState.TOMORROW]
 * - hoje + 2 a hoje + 7 dias → [DeadlineState.SOON]
 * - mais de 7 dias ou sem data → `null` (sem indicador)
 *
 * Não lê relógio: recebe `today` do [com.foli.listadetarefas.core.time.TimeProvider]
 * para manter testes determinísticos e o recálculo sob controle da UI.
 */
object DeadlineClassifier {

    fun classify(today: LocalDate, dueDate: LocalDate?): DeadlineState? {
        if (dueDate == null) return null
        return when (val days = ChronoUnit.DAYS.between(today, dueDate)) {
            0L -> DeadlineState.TODAY
            1L -> DeadlineState.TOMORROW
            in 2L..7L -> DeadlineState.SOON
            else -> if (days < 0L) DeadlineState.OVERDUE else null
        }
    }
}
