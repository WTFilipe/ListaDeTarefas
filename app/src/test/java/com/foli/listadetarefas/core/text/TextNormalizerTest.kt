package com.foli.listadetarefas.core.text

import org.junit.Assert.assertEquals
import org.junit.Test

class TextNormalizerTest {

    @Test
    fun normalize_ignoresCaseAndAccents() {
        assertEquals("acao", TextNormalizer.normalize("  AÇÃO  "))
        assertEquals("joao", TextNormalizer.normalize("João"))
    }

    @Test
    fun normalize_keepsInternalSpacing() {
        assertEquals("minha etiqueta", TextNormalizer.normalize("Minha Etiqueta"))
    }
}
