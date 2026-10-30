package com.foli.listadetarefas.data.local.db

import kotlinx.coroutines.flow.Flow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verificação estrutural do contrato do DAO.
 *
 * O comportamento real de SQLite é exercitado pelo [TaskDao] em tempo de execução
 * no aparelho. O ambiente de testes JVM deste projeto **não** inclui Robolectric
 * (dependência não prevista no catálogo da T01), portanto não é possível abrir um
 * `Room.inMemoryDatabaseBuilder` aqui. A limitação está registrada no
 * `IMPLEMENTATION.md`; a leitura reativa e a idempotência são cobertas por
 * `TaskRepositoryImplTest` com um duplê fiel do DAO.
 */
class TaskDaoTest {

    @Test
    fun dao_exposesReactiveReadsAndIdempotentWrites() {
        val methods = TaskDao::class.java.declaredMethods.map { it.name }.toSet()

        assertTrue(methods.containsAll(listOf("observeAll", "observeById", "getById", "upsert", "deleteById")))
    }

    @Test
    fun observeAll_returnsFlow() {
        val method = TaskDao::class.java.getDeclaredMethod("observeAll")

        assertEquals(Flow::class.java, method.returnType)
    }

    @Test
    fun observeById_returnsFlow() {
        val method = TaskDao::class.java.getDeclaredMethod("observeById", String::class.java)

        assertEquals(Flow::class.java, method.returnType)
    }
}
