package com.exemple.reveil.ui.theme.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exemple.reveil.ui.theme.ReveilTheme

@Composable
fun NouveauGrp(
    modifier: Modifier = Modifier,
    viewModel: NouveauGrpViewModel
) {
    val context = LocalContext.current

    val timePickerDialog = remember(viewModel.heure, viewModel.minute) {
        TimePickerDialog(
            context,
            { _, h: Int, m: Int -> viewModel.updateHeure(h, m) },
            viewModel.heure,
            viewModel.minute,
            true
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Créer un nouveau groupe",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = viewModel.nomGroupe,
                onValueChange = { viewModel.onNomGroupeChange(it) },
                label = { Text("Nom du groupe") },
                placeholder = { Text("Ex: Les lève-tôt") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            AlarmeCard(
                heureAffichee = viewModel.heureAffichee,
                alarmeActivee = viewModel.alarmeActivee,
                onAlarmeToggle = { viewModel.onAlarmeToggle(it) },
                onHeureClick = { timePickerDialog.show() }
            )

            OutlinedTextField(
                value = viewModel.champOvale,
                onValueChange = { viewModel.onChampOvaleChange(it) },
                placeholder = { Text("Ajouter des amis") },
                shape = RoundedCornerShape(50),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                viewModel.listeAmis.forEachIndexed { index, ami ->
                    AmiItem(
                        ami = ami,
                        onSelectionChange = { isChecked ->
                            viewModel.toggleAmiSelection(index, isChecked)
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.creerGroupe() },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("✓", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("✕", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AlarmeCard(
    heureAffichee: String,
    alarmeActivee: Boolean,
    onAlarmeToggle: (Boolean) -> Unit,
    onHeureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Alarme",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clickable(enabled = alarmeActivee) {
                    onHeureClick()
                }
            ) {
                Text(
                    text = heureAffichee,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (alarmeActivee) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Switch(
                checked = alarmeActivee,
                onCheckedChange = onAlarmeToggle
            )
        }
    }
}

@Composable
fun AmiItem(
    ami: Ami,
    onSelectionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelectionChange(!ami.selectionne) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = ami.selectionne,
            onCheckedChange = { isChecked -> onSelectionChange(isChecked) }
        )
        Text(
            text = ami.nom,
            fontSize = 17.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NouveauGrpPreview() {
    ReveilTheme {
        NouveauGrp(viewModel = NouveauGrpViewModel())
    }
}