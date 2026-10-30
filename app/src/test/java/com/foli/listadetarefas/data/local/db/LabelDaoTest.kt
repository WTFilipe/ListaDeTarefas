package com.foli.listadetarefas.data.local.db

import kotlinx.coroutines.flow.Flow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verificação estrutural do dao de etiquetas. O ambiente JVM não inclui
 * Robolectric; o comportamento transacional é coberto indiretamente pelo
 * repositório e pelo checklist de persistência da história.
 */
class LabelDaoTest {

    @Test
    fun dao_exposesReactiveReadAndUniqueKeyLookup() {
        val methods = LabelDao::class.java.declaredMethods.map { it.name }.toSet()

        assertTrue(
            methods.containsAll(listOf("observeAll", "getById", "getByNameKey", "upsert", "deleteById")),
        )
    }

    @Test
    fun observeAll_returnsFlow() {
        val method = LabelDao::class.java.getDeclaredMethod("observeAll")

        assertEquals(Flow::class.java, method.returnType)
    }
}
