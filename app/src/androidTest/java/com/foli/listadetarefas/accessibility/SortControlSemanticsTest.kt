package com.foli.listadetarefas.accessibility

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.foli.listadetarefas.R
import com.foli.listadetarefas.domain.model.SortOrder
import com.foli.listadetarefas.ui.designsystem.components.SortControl
import com.foli.listadetarefas.ui.feature.tasks.presentation.components.TaskSortSheet
import com.foli.listadetarefas.ui.feature.tasks.presentation.labelRes
import com.foli.listadetarefas.ui.theme.ListaDeTarefasTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Semântica da ordenação (US-07 AC 1/9): o controle anuncia o nome completo da
 * ordenação vigente e o S5 expõe as seis opções em radio com a atual marcada
 * (DESIGN_SPEC §10/§13.2).
 */
@RunWith(AndroidJUnit4::class)
class SortControlSemanticsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val optionName: String = context.getString(R.string.sort_created_desc)

    private val label: String = context.getString(R.string.sort_label, optionName)

    @Test
    fun sortControl_announcesOnlyTheOrderName_andOpensSheet() {
        // Copy exata da DESIGN_SPEC §10/§11: "Ordenação atual: <nome>", sem
        // repetir o prefixo "Ordenação:" do texto visível (M2).
        val description = context.getString(R.string.a11y_sort_current, optionName)
        var clicked = false

        composeRule.setContent {
            ListaDeTarefasTheme {
                SortControl(
                    label = label,
                    currentOption = optionName,
                    onClick = { clicked = true },
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(description)
            .assertIsDisplayed()
            .performClick()

        assertTrue(clicked)
        // O texto visível continua sendo o rótulo completo do botão.
        composeRule.onNodeWithText(label).assertIsDisplayed()
    }

    @Test
    fun sortControl_acceptsFocusBackFromTheSheet() {
        // Base do M3: o controle é focável e aceita a devolução de foco feita
        // pela tela quando o S5 fecha (DESIGN_SPEC §9.5).
        val focusRequester = FocusRequester()

        composeRule.setContent {
            ListaDeTarefasTheme {
                Column {
                    SortControl(
                        label = label,
                        currentOption = optionName,
                        onClick = {},
                        modifier = Modifier.focusRequester(focusRequester),
                    )
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                }
            }
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.a11y_sort_current, optionName))
            .assertIsFocused()
    }

    @Test
    fun sortSheet_showsAllSixOptionsAndSelectsOne() {
        var selected: SortOrder? = null

        composeRule.setContent {
            ListaDeTarefasTheme {
                TaskSortSheet(
                    selected = SortOrder.CREATED_DESC,
                    onSelect = { selected = it },
                    onDismiss = {},
                )
            }
        }

        composeRule
            .onNodeWithText(context.getString(R.string.sort_sheet_title))
            .assertIsDisplayed()

        SortOrder.entries.forEach { order ->
            composeRule
                .onNodeWithText(context.getString(order.labelRes()))
                .assertIsDisplayed()
        }

        composeRule
            .onNodeWithText(context.getString(R.string.sort_created_desc))
            .assertIsSelected()

        composeRule
            .onNodeWithText(context.getString(R.string.sort_title_az))
            .performClick()

        assertEquals(SortOrder.TITLE_ASC, selected)
    }
}
