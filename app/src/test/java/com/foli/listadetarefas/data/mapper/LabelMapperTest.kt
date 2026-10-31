package com.foli.listadetarefas.data.mapper

import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.data.local.db.LabelEntity
import com.foli.listadetarefas.domain.model.TaskLabel
import org.junit.Assert.assertEquals
import org.junit.Test

class LabelMapperTest {

    @Test
    fun roundTrip_preservesAllFields() {
        val label = TaskLabel("id-1", "Trabalho", "trabalho", LabelColorKey.BLUE, 100L)

        val entity = LabelMapper.toEntity(label)
        val domain = LabelMapper.toDomain(entity)

        assertEquals(label, domain)
        assertEquals("BLUE", entity.colorKey)
    }

    @Test
    fun unknownColorKey_fallsBackToRed() {
        val entity = LabelEntity("id-1", "Antiga", "antiga", "MAGENTA", 1L)

        assertEquals(LabelColorKey.RED, LabelMapper.toDomain(entity).colorKey)
    }
}
