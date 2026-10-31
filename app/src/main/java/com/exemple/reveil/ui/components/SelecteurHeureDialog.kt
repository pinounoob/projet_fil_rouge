package com.exemple.reveil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun SelecteurHeureDialog(
    heureInitiale: Int,
    minuteInitiale: Int,
    onValider: (heure: Int, minute: Int) -> Unit,
    onAnnuler: () -> Unit
) {
    var heure by remember { mutableIntStateOf(heureInitiale) }
    var minute by remember { mutableIntStateOf(minuteInitiale) }

    Dialog(onDismissRequest = onAnnuler) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Text("Heure du réveil", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Molette(valeur = heure,
                        onPlus = { heure = (heure + 1) % 24 },
                        onMoins = { heure = if (heure > 0) heure - 1 else 23 })
                    Text(":", fontSize = 42.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    Molette(valeur = minute,
                        onPlus = { minute = (minute + 1) % 60 },
                        onMoins = { minute = if (minute > 0) minute - 1 else 59 })
                }
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onAnnuler) { Text("ANNULER", color = MaterialTheme.colorScheme.error) }
                    Button(onClick = { onValider(heure, minute) }, shape = RoundedCornerShape(12.dp)) {
                        Text("VALIDER", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun Molette(valeur: Int, onPlus: () -> Unit, onMoins: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextButton(onClick = onPlus) { Text("▲", fontSize = 22.sp, color = MaterialTheme.colorScheme.primary) }
        Text("%02d".format(valeur), fontSize = 42.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = onMoins) { Text("▼", fontSize = 22.sp, color = MaterialTheme.colorScheme.primary) }
    }
}