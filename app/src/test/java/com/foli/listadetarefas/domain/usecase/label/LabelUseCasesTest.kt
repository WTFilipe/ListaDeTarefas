package com.foli.listadetarefas.domain.usecase.label

import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.domain.model.TaskLabel
import com.foli.listadetarefas.testing.FakeLabelRepository
import com.foli.listadetarefas.testing.FakeTimeProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LabelUseCasesTest {

    private val time = FakeTimeProvider(nowMillisValue = 700L)

    @Test
    fun create_persistsNormalizedNameAndColor() = runTest {
        val repository = FakeLabelRepository()

        val result = CreateLabelUseCase(repository, time)("id-1", "  Trabalho  ", LabelColorKey.RED)

        val label = (result as AppResult.Success).data
        assertEquals("Trabalho", label.name)
        assertEquals("trabalho", label.nameKey)
        assertEquals(700L, label.createdAt)
        assertEquals(1, repository.upsertCalls)
    }

    @Test
    fun create_blocksDuplicateIgnoringCaseAndAccents() = runTest {
        val repository = FakeLabelRepository().apply {
            labels.value = listOf(
                TaskLabel("id-1", "Ação", "acao", LabelColorKey.RED, 1L),
            )
        }

        val result = CreateLabelUseCase(repository, time)("id-2", "AÇÃO", LabelColorKey.BLUE)

        assertEquals(AppFailure.DuplicateLabelName, (result as AppResult.Failure).failure)
    }

    @Test
    fun update_keepsCreatedAtAndRejectsDuplicateOfAnotherLabel() = runTest {
        val repository = FakeLabelRepository().apply {
            labels.value = listOf(
                TaskLabel("id-1", "Trabalho", "trabalho", LabelColorKey.RED, 1L),
                TaskLabel("id-2", "Estudos", "estudos", LabelColorKey.BLUE, 2L),
            )
        }

        val updated = UpdateLabelUseCase(repository)("id-1", "Estudos", LabelColorKey.GREEN)
        assertEquals(AppFailure.DuplicateLabelName, (updated as AppResult.Failure).failure)

        val ok = UpdateLabelUseCase(repository)("id-1", "Pessoal", LabelColorKey.GREEN)
        val label = (ok as AppResult.Success).data
        assertEquals("Pessoal", label.name)
        assertEquals(1L, label.createdAt)
    }

    @Test
    fun delete_callsRepositoryOnce() = runTest {
        val repository = FakeLabelRepository()

        val result = DeleteLabelUseCase(repository)("id-1")

        assertTrue(result is AppResult.Success)
        assertEquals(1, repository.deleteCalls)
    }
}
