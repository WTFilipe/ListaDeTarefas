package com.foli.listadetarefas.accessibility

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.foli.listadetarefas.R
import com.foli.listadetarefas.ui.designsystem.components.TaskRow
import com.foli.listadetarefas.ui.feature.tasks.presentation.components.DeleteTaskDialog
import com.foli.listadetarefas.ui.model.DeadlineUiState
import com.foli.listadetarefas.ui.model.TaskUiModel
import com.foli.listadetarefas.ui.theme.ListaDeTarefasTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Semântica da linha da tarefa (US-01 AC 10): o truncamento visual (2 linhas) não
 * pode perder informação no TalkBack — o `contentDescription` carrega o texto
 * integral. Também cobre o menu ⋮ e o dialog S6 (T06).
 */
@RunWith(AndroidJUnit4::class)
class TaskRowSemanticsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun task(title: String, description: String) = TaskUiModel(
        id = "id-1",
        title = title,
        description = description,
    )

    @Test
    fun row_announcesFullTextEvenWhenVisuallyTruncated() {
        val longTitle = "Título muito longo ".repeat(8)
        val longDescription = "Descrição muito longa ".repeat(16)

        composeRule.setContent {
            ListaDeTarefasTheme {
                TaskRow(
                    task = task(longTitle, longDescription),
                    onClick = {},
                    onEdit = {},
                    onDelete = {},
                    onToggle = {},
                )
            }
        }

        val expected = longTitle + ". " + longDescription
        composeRule.onNodeWithContentDescription(expected).assertIsDisplayed()
    }

    @Test
    fun row_withoutDescription_announcesOnlyTheTitle() {
        composeRule.setContent {
            ListaDeTarefasTheme {
                TaskRow(
                    task = task("Ligar para o dentista", ""),
                    onClick = {},
                    onEdit = {},
                    onDelete = {},
                    onToggle = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Ligar para o dentista").assertIsDisplayed()
    }

    @Test
    fun completedRow_announcesCompletionDateAndStrikethrough() {
        composeRule.setContent {
            ListaDeTarefasTheme {
                TaskRow(
                    task = TaskUiModel(
                        id = "id-2",
                        title = "Comprar leite",
                        description = "No mercado",
                        isCompleted = true,
                        completedOnDate = "12/11/2026",
                    ),
                    onClick = {},
                    onEdit = {},
                    onDelete = {},
                    onToggle = {},
                )
            }
        }

        val expected = context.getString(
            R.string.a11y_task,
            context.getString(R.string.a11y_task, "Comprar leite", "No mercado"),
            context.getString(R.string.completed_on_a11y, "12/11/2026"),
        )
        composeRule.onNodeWithContentDescription(expected).assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.completed_on, "12/11/2026"))
            .assertIsDisplayed()
    }

    @Test
    fun overdueRow_announcesDeadlineTogetherWithTitle() {
        composeRule.setContent {
            ListaDeTarefasTheme {
                TaskRow(
                    task = TaskUiModel(
                        id = "id-3",
                        title = "Comprar leite",
                        description = "No mercado",
                        deadline = DeadlineUiState.OVERDUE,
                        deadlineDateLabel = "29/10/2026",
                    ),
                    onClick = {},
                    onEdit = {},
                    onDelete = {},
                    onToggle = {},
                )
            }
        }

        val expected = context.getString(
            R.string.a11y_task,
            context.getString(R.string.a11y_task, "Comprar leite", "No mercado"),
            context.getString(R.string.deadline_a11y_overdue, "29/10/2026"),
        )
        composeRule.onNodeWithContentDescription(expected).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.deadline_overdue)).assertIsDisplayed()
    }

    @Test
    fun completedRow_doesNotShowDeadlineBadge() {
        composeRule.setContent {
            ListaDeTarefasTheme {
                TaskRow(
                    task = TaskUiModel(
                        id = "id-4",
                        title = "Concluída",
                        description = "",
                        isCompleted = true,
                        completedOnDate = "12/11/2026",
                    ),
                    onClick = {},
                    onEdit = {},
                    onDelete = {},
                    onToggle = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.deadline_overdue)).assertDoesNotExist()
        composeRule.onNodeWithText(context.getString(R.string.completed_on, "12/11/2026"))
            .assertIsDisplayed()
    }

    @Test
    fun moreOptionsMenu_exposesEditAndDelete() {
        composeRule.setContent {
            ListaDeTarefasTheme {
                TaskRow(
                    task = task("Comprar leite", "No mercado"),
                    onClick = {},
                    onEdit = {},
                    onDelete = {},
                    onToggle = {},
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.a11y_more_options))
            .performClick()

        composeRule.onNodeWithText(context.getString(R.string.menu_edit)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.delete)).assertIsDisplayed()
    }

    @Test
    fun deleteDialog_confirmsOnlyThroughExplicitAction() {
        var confirmed = false
        var dismissed = false

        composeRule.setContent {
            ListaDeTarefasTheme {
                DeleteTaskDialog(
                    onConfirm = { confirmed = true },
                    onDismiss = { dismissed = true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.delete_task_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.delete_task_body)).assertIsDisplayed()

        composeRule.onNodeWithText(context.getString(R.string.cancel)).performClick()
        assertTrue(dismissed)
        assertTrue(!confirmed)

        composeRule.onNodeWithText(context.getString(R.string.delete)).performClick()
        assertTrue(confirmed)
    }
}
