package com.diego.littly.core.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.diego.littly.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LittlyTimePickerDialog(
    initialMinuteOfDay: Int?,
    onConfirm: (minuteOfDay: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val initial = initialMinuteOfDay ?: 9 * 60
    val state = rememberTimePickerState(
        initialHour = initial / 60,
        initialMinute = initial % 60,
        is24Hour = android.text.format.DateFormat.is24HourFormat(
            androidx.compose.ui.platform.LocalContext.current
        )
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        text = { TimePicker(state = state) }
    )
}
