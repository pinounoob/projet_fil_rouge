package com.exemple.reveil.ui.components

import com.exemple.reveil.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecteurHeureDialog(
    heureInitiale: Int,
    minuteInitiale: Int,
    onValider: (heure: Int, minute: Int) -> Unit,
    onAnnuler: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = heureInitiale,
        initialMinute = minuteInitiale,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onAnnuler,
        title = {
            Text(
                text = stringResource(R.string.heure_du_reveil),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            Button(
                onClick = { onValider(timePickerState.hour, timePickerState.minute) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.valider))
            }
        },
        dismissButton = {
            TextButton(onClick = onAnnuler) {
                Text(stringResource(R.string.annuler))
            }
        }
    )
}
