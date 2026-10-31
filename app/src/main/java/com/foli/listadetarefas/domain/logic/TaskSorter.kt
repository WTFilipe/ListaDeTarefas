package com.foli.listadetarefas.domain.logic

import com.foli.listadetarefas.domain.model.SortOrder
import com.foli.listadetarefas.domain.model.Task
import java.text.Collator
import java.util.Locale

/**
 * Ordenação pura da lista (US-07), aplicada depois dos filtros de aba e busca.
 *
 * Regras:
 *
 * - **Título** usa `java.text.Collator` com `Locale pt-BR` e força `PRIMARY`
 *   (ADR-0008), ignorando caixa e acentos, e desempata por criação mais recente
 *   (US-07 AC 5).
 * - **Criação** usa somente `createdAt` — edição e conclusão não influenciam
 *   (US-07 AC 6).
 * - **Prazo** coloca tarefas sem data **sempre ao final**, nas duas direções, e
 *   desempata por criação mais recente (US-07 AC 7).
 * - O desempate final é determinístico por `id`, independente da ordem de
 *   entrada. O default é [SortOrder.DEFAULT] (criação mais recente primeiro).
 */
object TaskSorter {

    private val LOCALE_PT_BR: Locale = Locale.forLanguageTag("pt-BR")

    fun sort(
        items: List<Task>,
        order: SortOrder = SortOrder.DEFAULT,
    ): List<Task> = when (order) {
        SortOrder.TITLE_ASC -> items.sortedWith(titleComparator(ascending = true))
        SortOrder.TITLE_DESC -> items.sortedWith(titleComparator(ascending = false))
        SortOrder.CREATED_DESC -> items.sortedWith(creationDescending())
        SortOrder.CREATED_ASC -> items.sortedWith(
            compareBy<Task> { it.createdAt }.thenBy { it.id },
        )
        SortOrder.DEADLINE_ASC -> items.sortedWith(deadlineComparator(ascending = true))
        SortOrder.DEADLINE_DESC -> items.sortedWith(deadlineComparator(ascending = false))
    }

    private fun creationDescending(): Comparator<Task> =
        compareByDescending<Task> { it.createdAt }.thenBy { it.id }

    private fun titleComparator(ascending: Boolean): Comparator<Task> {
        // `Collator` não é thread-safe: cada ordenação cria a sua instância
        // (ADR-0008: pt-BR com força PRIMARY, que ignora caixa e acentos).
        val collator = Collator.getInstance(LOCALE_PT_BR).apply {
            strength = Collator.PRIMARY
        }
        val byTitle = Comparator<Task> { first, second ->
            val result = collator.compare(first.title, second.title)
            if (ascending) result else invert(result)
        }
        return byTitle.thenByDescending { it.createdAt }.thenBy { it.id }
    }

    /** Inverte o resultado sem risco de overflow em `Int.MIN_VALUE`. */
    private fun invert(result: Int): Int = when {
        result < 0 -> 1
        result > 0 -> -1
        else -> 0
    }

    private fun deadlineComparator(ascending: Boolean): Comparator<Task> =
        Comparator<Task> { first, second ->
            val left = first.dueDate
            val right = second.dueDate
            when {
                left == null && right == null -> 0
                left == null -> 1
                right == null -> -1
                ascending -> left.compareTo(right)
                else -> right.compareTo(left)
            }
        }
            .thenByDescending { it.createdAt }
            .thenBy { it.id }
}
