package com.invex.one.template.core.designsystem.molecule

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Material date picker themed by [com.invex.one.template.core.designsystem.theme.AppTheme].
 * Values are UTC-midnight millis; [pastOnly] disables future dates (birthdates).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialMillis: Long?,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: (Long?) -> Unit,
    onDismiss: () -> Unit,
    pastOnly: Boolean = true,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = !pastOnly || utcTimeMillis <= System.currentTimeMillis()
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onConfirm(pickerState.selectedDateMillis) }) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissLabel) } },
    ) { DatePicker(state = pickerState) }
}

/** Formats UTC-midnight millis (date picker values) without shifting the day. */
fun formatUtcDate(millis: Long, pattern: String): String =
    SimpleDateFormat(pattern, Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(millis))
