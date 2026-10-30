package com.foli.listadetarefas.core.format

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formatação de data local do produto (`dd/MM/aaaa`, pt-BR), usada pela marca
 * "Concluída em" (US-03 AC 3) e pelo chip/marca da data limite (US-05).
 * Não lê o relógio: recebe o instante/data já persistido e apenas converte.
 */
object DateLabelFormatter {

    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))

    fun format(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(epochMillis)
            .atZone(zoneId)
            .toLocalDate()
            .format(formatter)

    /** Formata uma data local já sem hora (o prazo é somente data). */
    fun formatDate(date: LocalDate): String = date.format(formatter)
}
