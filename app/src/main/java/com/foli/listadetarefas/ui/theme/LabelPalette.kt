package com.foli.listadetarefas.ui.theme

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.foli.listadetarefas.R
import com.foli.listadetarefas.core.palette.LabelColorKey

/** Cores de uma etiqueta da paleta fixa (`COLOR_SCHEME.md` §6). */
@Immutable
data class LabelColors(
    val container: Color,
    val onContainer: Color,
    val dot: Color,
)

/** Entrada da paleta: chave estável + nome localizado + cores do tema atual. */
@Immutable
data class LabelColorStyle(
    val key: LabelColorKey,
    val colors: LabelColors,
    @param:StringRes val nameRes: Int,
)

/**
 * Paleta fixa de 8 cores. Não muda com dynamic color e é independente do
 * fabricante; a ordem é a normativa do design (Vermelho primeiro).
 */
@Immutable
class LabelPalette(val styles: List<LabelColorStyle>) {
    private val byKey: Map<LabelColorKey, LabelColorStyle> = styles.associateBy { it.key }

    fun styleFor(key: LabelColorKey): LabelColorStyle = byKey[key] ?: styles.first()

    fun colorsFor(key: LabelColorKey): LabelColors = styleFor(key).colors
}

/**
 * CompositionLocal da paleta de etiquetas, fornecida pelo tema. O design system
 * nunca importa `domain`; ele consome apenas `ui/model` + esta paleta.
 */
val LocalLabelPalette = staticCompositionLocalOf<LabelPalette> {
    error("LocalLabelPalette não fornecido pelo ListaDeTarefasTheme")
}

internal fun lightLabelPalette(): LabelPalette = LabelPalette(
    styles = listOf(
        LabelColorStyle(LabelColorKey.RED, LabelColors(Color(0xFFFFDAD6), Color(0xFF410002), Color(0xFFB3261E)), R.string.color_name_red),
        LabelColorStyle(LabelColorKey.ORANGE, LabelColors(Color(0xFFFFDCC5), Color(0xFF3B1A00), Color(0xFF9A4500)), R.string.color_name_orange),
        LabelColorStyle(LabelColorKey.MUSTARD, LabelColors(Color(0xFFF8E3AE), Color(0xFF3A2E00), Color(0xFF8A6A00)), R.string.color_name_mustard),
        LabelColorStyle(LabelColorKey.GREEN, LabelColors(Color(0xFFC8EBC6), Color(0xFF0B2B0A), Color(0xFF2F6B2D)), R.string.color_name_green),
        LabelColorStyle(LabelColorKey.TEAL, LabelColors(Color(0xFFC2ECE7), Color(0xFF033731), Color(0xFF00695F)), R.string.color_name_teal),
        LabelColorStyle(LabelColorKey.BLUE, LabelColors(Color(0xFFD2E4FA), Color(0xFF0B2A47), Color(0xFF175A94)), R.string.color_name_blue),
        LabelColorStyle(LabelColorKey.PURPLE, LabelColors(Color(0xFFE5DDFF), Color(0xFF211043), Color(0xFF5B3FA8)), R.string.color_name_purple),
        LabelColorStyle(LabelColorKey.PINK, LabelColors(Color(0xFFFFD9E3), Color(0xFF3B0718), Color(0xFFA32B54)), R.string.color_name_pink),
    ),
)

internal fun darkLabelPalette(): LabelPalette = LabelPalette(
    styles = listOf(
        LabelColorStyle(LabelColorKey.RED, LabelColors(Color(0xFF93000A), Color(0xFFFFDAD6), Color(0xFFFFB4AB)), R.string.color_name_red),
        LabelColorStyle(LabelColorKey.ORANGE, LabelColors(Color(0xFF7A3500), Color(0xFFFFDCC5), Color(0xFFFFB784)), R.string.color_name_orange),
        LabelColorStyle(LabelColorKey.MUSTARD, LabelColors(Color(0xFF564500), Color(0xFFF8E3AE), Color(0xFFE2C46B)), R.string.color_name_mustard),
        LabelColorStyle(LabelColorKey.GREEN, LabelColors(Color(0xFF1B4D1A), Color(0xFFC8EBC6), Color(0xFFA5D6A3)), R.string.color_name_green),
        LabelColorStyle(LabelColorKey.TEAL, LabelColors(Color(0xFF00504A), Color(0xFFC2ECE7), Color(0xFF9FE0D9)), R.string.color_name_teal),
        LabelColorStyle(LabelColorKey.BLUE, LabelColors(Color(0xFF0B4A74), Color(0xFFD2E4FA), Color(0xFF9FCAFF)), R.string.color_name_blue),
        LabelColorStyle(LabelColorKey.PURPLE, LabelColors(Color(0xFF3A2A6B), Color(0xFFE5DDFF), Color(0xFFCBBEFF)), R.string.color_name_purple),
        LabelColorStyle(LabelColorKey.PINK, LabelColors(Color(0xFF641F30), Color(0xFFFFD9E3), Color(0xFFFFB1C3)), R.string.color_name_pink),
    ),
)
