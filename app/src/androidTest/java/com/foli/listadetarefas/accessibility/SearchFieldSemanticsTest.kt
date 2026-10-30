package com.foli.listadetarefas.accessibility

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.foli.listadetarefas.R
import com.foli.listadetarefas.ui.designsystem.components.EmptyState
import com.foli.listadetarefas.ui.designsystem.components.SearchField
import com.foli.listadetarefas.ui.designsystem.icons.AppIcons
import com.foli.listadetarefas.ui.theme.ListaDeTarefasTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Semântica da busca (US-06 AC 1/6): o "limpar" tem descrição própria e alvo
 * interativo, e o eco da consulta no estado vazio é acessível **integralmente**
 * mesmo quando truncado visualmente em 2 linhas (DESIGN_SPEC §4/§10).
 */
@RunWith(AndroidJUnit4::class)
class SearchFieldSemanticsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun searchField_showsPlaceholderAndClearActionOnlyWithQuery() {
        var cleared = false

        composeRule.setContent {
            ListaDeTarefasTheme {
                SearchField(
                    query = "leite",
                    onQueryChange = {},
                    onClear = { cleared = true },
                )
            }
        }

        composeRule
            .onNodeWithText(context.getString(R.string.search_placeholder))
            .assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.search_clear))
            .assertIsDisplayed()
            .performClick()
        assertTrue(cleared)
    }

    @Test
    fun searchField_withoutQuery_hasNoClearAction() {
        composeRule.setContent {
            ListaDeTarefasTheme {
                SearchField(
                    query = "",
                    onQueryChange = {},
                    onClear = {},
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.search_clear))
            .assertDoesNotExist()
    }

    @Test
    fun searchEmptyState_announcesFullQueryEvenWhenVisuallyTruncated() {
        val longQuery = "revisar proposta do cliente com prazo ".repeat(6).trim()
        val echo = context.getString(R.string.empty_search_echo, longQuery)
        var cleared = false

        composeRule.setContent {
            ListaDeTarefasTheme {
                EmptyState(
                    title = context.getString(R.string.empty_search_title),
                    echo = echo,
                    body = context.getString(R.string.empty_search_body),
                    icon = AppIcons.Search,
                    actionLabel = context.getString(R.string.search_clear),
                    onAction = { cleared = true },
                    isActionTonal = true,
                )
            }
        }

        composeRule.onNodeWithContentDescription(echo).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.empty_search_title))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.search_clear)).performClick()
        assertTrue(cleared)
    }
}
