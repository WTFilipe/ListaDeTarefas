package com.foli.listadetarefas.core.logging

import android.util.Log
import com.foli.listadetarefas.BuildConfig

/**
 * Wrapper de log que só emite em build de debug. Nunca recebe título,
 * descrição, nome de etiqueta ou consulta de busca.
 */
object AppLogger {
    private const val TAG = "ListaDeTarefas"

    fun debug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    fun warn(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, message, throwable)
        }
    }
}
