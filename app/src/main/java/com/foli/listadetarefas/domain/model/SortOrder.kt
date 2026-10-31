package com.foli.listadetarefas.domain.model

/**
 * Opções de ordenação da lista (US-07). O nome do enum é o valor persistido na
 * chave `sort_order` do DataStore (`ARCHITECTURE.md` §9.2) e o parâmetro do
 * evento `sort_changed` (`ARCHITECTURE.md` §10).
 *
 * [DEFAULT] é "criação mais recente primeiro", a ordenação vigente desde a US-01.
 * Este pacote é Kotlin puro: a copy localizada de cada opção pertence à UI.
 */
enum class SortOrder {
    TITLE_ASC,
    TITLE_DESC,
    CREATED_DESC,
    CREATED_ASC,
    DEADLINE_ASC,
    DEADLINE_DESC;

    companion object {
        val DEFAULT: SortOrder = CREATED_DESC

        /** Converte o valor persistido; ausente/desconhecido cai no default. */
        fun fromName(name: String?): SortOrder =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
