package com.foli.listadetarefas.data.mapper

import com.foli.listadetarefas.domain.model.TaskTab
import org.junit.Assert.assertEquals
import org.junit.Test

class PreferencesMapperTest {

    @Test
    fun nullValue_fallsBackToActiveTab() {
        assertEquals(TaskTab.ACTIVE, PreferencesMapper.toDomain(null).selectedTab)
    }

    @Test
    fun unknownValue_fallsBackToActiveTab() {
        assertEquals(TaskTab.ACTIVE, PreferencesMapper.toDomain("SOMETHING_ELSE").selectedTab)
    }

    @Test
    fun knownValue_isRestored() {
        assertEquals(TaskTab.COMPLETED, PreferencesMapper.toDomain("COMPLETED").selectedTab)
    }
}
