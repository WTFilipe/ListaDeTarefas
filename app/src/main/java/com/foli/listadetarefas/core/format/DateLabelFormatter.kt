package com.foli.listadetarefas.core.format

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formatação de data local do produto (`dd/MM/aaaa`, pt-BR), usada pela marca
 * "Concluída em" (US-03 AC 3). Não lê o relógio: recebe o instante já persistido
 * e apenas converte para a data local.
 */
object DateLabelFormatter {

    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))

    fun format(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(epochMillis)
            .atZone(zoneId)
            .toLocalDate()
            .format(formatter)
}
