package com.foli.listadetarefas.accessibility

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.foli.listadetarefas.R
import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.ui.designsystem.components.LabelChip
import com.foli.listadetarefas.ui.model.LabelUiModel
import com.foli.listadetarefas.ui.theme.ListaDeTarefasTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Semântica do chip de etiqueta (US-04 AC 10–12): o nome completo é anunciado
 * mesmo quando o texto é truncado visualmente; a cor nunca é o único sinal.
 */
@RunWith(AndroidJUnit4::class)
class LabelChipSemanticsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun longLabelName_isAnnouncedInFullWhenVisuallyTruncated() {
        val longName = "Etiqueta muito longa ".repeat(6)

        composeRule.setContent {
            ListaDeTarefasTheme {
                LabelChip(
                    label = LabelUiModel("l1", longName, LabelColorKey.PURPLE),
                )
            }
        }

        val expected = context.getString(R.string.a11y_label, longName)
        composeRule.onNodeWithContentDescription(expected).assertIsDisplayed()
    }

    @Test
    fun shortLabelName_isVisibleAsText() {
        composeRule.setContent {
            ListaDeTarefasTheme {
                LabelChip(label = LabelUiModel("l1", "Trabalho", LabelColorKey.BLUE))
            }
        }

        composeRule.onNodeWithText("Trabalho").assertIsDisplayed()
    }
}
