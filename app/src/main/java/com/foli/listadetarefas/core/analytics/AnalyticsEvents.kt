package com.foli.listadetarefas.core.analytics

/**
 * Lista normativa de eventos (`ARCHITECTURE.md` §10). Nomes em snake_case, sem
 * PII.
 */
object AnalyticsEvents {
    const val TASK_CREATED_SUCCESS = "task_created_success"
    const val TASK_UPDATED = "task_updated"
    const val TASK_DELETED = "task_deleted"
    const val TASK_COMPLETED = "task_completed"
    const val TASK_REOPENED = "task_reopened"
    const val LABEL_CREATED = "label_created"
    const val LABEL_UPDATED = "label_updated"
    const val LABEL_DELETED = "label_deleted"
    const val TASK_LABEL_CHANGED = "task_label_changed"
    const val DEADLINE_SET = "deadline_set"
    const val DEADLINE_CLEARED = "deadline_cleared"
    const val SEARCH_PERFORMED = "search_performed"
    const val SEARCH_CLEARED = "search_cleared"
    const val SORT_CHANGED = "sort_changed"
}
