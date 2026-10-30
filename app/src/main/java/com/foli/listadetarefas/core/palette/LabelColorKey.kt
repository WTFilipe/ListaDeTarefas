package com.foli.listadetarefas.core.palette

/**
 * Paleta fixa de etiquetas (`COLOR_SCHEME.md` §6). É um conjunto fechado de
 * chaves estáveis, sem qualquer dependência de Android: a tradução para cores e
 * nomes localizados acontece na UI (`ui/theme/LabelPalette`).
 *
 * A ordem é normativa: [RED] é a primeira cor e a pré-selecionada na criação.
 */
enum class LabelColorKey {
    RED,
    ORANGE,
    MUSTARD,
    GREEN,
    TEAL,
    BLUE,
    PURPLE,
    PINK,
    ;

    companion object {
        /** Primeira cor da paleta, usada como pré-seleção na criação (AC 2). */
        val DEFAULT: LabelColorKey = RED

        /** Fallback tolerante para dados antigos/desconhecidos. */
        fun fromStorageOrNull(value: String): LabelColorKey? =
            entries.firstOrNull { it.name == value }
    }
}
