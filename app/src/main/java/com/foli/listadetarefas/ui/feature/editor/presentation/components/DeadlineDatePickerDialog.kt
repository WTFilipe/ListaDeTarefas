package com.foli.listadetarefas.ui.feature.editor.presentation.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.foli.listadetarefas.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * S8 — seleção da data limite (US-05 AC 1 e 7). Somente data (sem hora),
 * locale pt-BR, calendário/entrada manual e datas passadas permitidas.
 * O Material 3 trabalha com milissegundos UTC para datas; a conversão mantém
 * exatamente o dia escolhido.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeadlineDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialMillis = remember(initialDate) {
        initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selected = state.selectedDateMillis
                    if (selected == null) {
                        onDismiss()
                    } else {
                        onDateSelected(
                            Instant.ofEpochMilli(selected)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate(),
                        )
                    }
                },
            ) {
                Text(text = stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    ) {
        DatePicker(state = state, showModeToggle = true)
    }
}
