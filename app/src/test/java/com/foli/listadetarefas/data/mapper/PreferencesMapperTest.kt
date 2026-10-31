package com.foli.listadetarefas.data.mapper

import com.foli.listadetarefas.domain.model.SortOrder
import com.foli.listadetarefas.domain.model.TaskTab
import org.junit.Assert.assertEquals
import org.junit.Test

class PreferencesMapperTest {

    @Test
    fun nullValue_fallsBackToActiveTabAndDefaultSort() {
        val preferences = PreferencesMapper.toDomain(null)

        assertEquals(TaskTab.ACTIVE, preferences.selectedTab)
        assertEquals(SortOrder.CREATED_DESC, preferences.sortOrder)
    }

    @Test
    fun unknownValue_fallsBackToActiveTabAndDefaultSort() {
        val preferences = PreferencesMapper.toDomain("SOMETHING_ELSE", "SOMETHING_ELSE")

        assertEquals(TaskTab.ACTIVE, preferences.selectedTab)
        assertEquals(SortOrder.CREATED_DESC, preferences.sortOrder)
    }

    @Test
    fun knownValues_areRestored() {
        val preferences = PreferencesMapper.toDomain("COMPLETED", "TITLE_ASC")

        assertEquals(TaskTab.COMPLETED, preferences.selectedTab)
        assertEquals(SortOrder.TITLE_ASC, preferences.sortOrder)
    }

    @Test
    fun everySortOrderName_roundTrips() {
        SortOrder.entries.forEach { order ->
            assertEquals(order, PreferencesMapper.sortOrderFromName(order.name))
        }
    }

    @Test
    fun sortOrderNameIsNull_fallsBackToCreationMostRecentFirst() {
        assertEquals(
            SortOrder.CREATED_DESC,
            PreferencesMapper.toDomain("ACTIVE", null).sortOrder,
        )
    }
}
