package com.foli.listadetarefas.core.result

/**
 * Resultado de uma operação de domínio. Nunca lança exceções de infraestrutura
 * para a camada de apresentação.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val failure: com.foli.listadetarefas.core.result.AppFailure) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}
