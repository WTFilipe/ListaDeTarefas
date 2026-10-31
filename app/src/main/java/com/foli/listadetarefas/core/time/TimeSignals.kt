package com.foli.listadetarefas.core.time

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sinais de recálculo de data/hora (US-05 AC 5). Não há timer de meia-noite:
 * a `MainActivity` emite quando o app retoma (`ON_RESUME`) e quando o sistema
 * envia `ACTION_DATE_CHANGED`, `ACTION_TIME_CHANGED` ou
 * `ACTION_TIMEZONE_CHANGED`. Os ViewModels combinam esse fluxo para reprojetar
 * o prazo sem trabalho periódico.
 */
@Singleton
class TimeSignals @Inject constructor() {

    private val _signals = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val signals: SharedFlow<Unit> = _signals.asSharedFlow()

    fun emit() {
        _signals.tryEmit(Unit)
    }
}
