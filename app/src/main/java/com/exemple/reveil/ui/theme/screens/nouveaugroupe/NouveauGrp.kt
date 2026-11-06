package com.exemple.reveil.ui.theme.screens.nouveaugroupe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exemple.reveil.ui.theme.ReveilTheme

@Composable
fun NouveauGrp(
    modifier: Modifier = Modifier,
    viewModel: NouveauGrpViewModel = viewModel(),
    onGroupeCree: () -> Unit = {},
    onAnnuler: () -> Unit = {}
) {
    val heureAffichee = "%02d:%02d".format(viewModel.heure, viewModel.minute)
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        var tempHeure by remember { mutableIntStateOf(viewModel.heure) }
        var tempMinute by remember { mutableIntStateOf(viewModel.minute) }

        Dialog(onDismissRequest = { showTimePicker = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Heure du réveil",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Molette Heures (+1 / -1)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            TextButton(onClick = { tempHeure = (tempHeure + 1) % 24 }) {
                                Text("▲", fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                text = "%02d".format(tempHeure),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { tempHeure = if (tempHeure > 0) tempHeure - 1 else 23 }) {
                                Text("▼", fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Text(
                            text = ":",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Molette Minutes (+1 / -1)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            TextButton(onClick = { tempMinute = (tempMinute + 1) % 60 }) {
                                Text("▲", fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                text = "%02d".format(tempMinute),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { tempMinute = if (tempMinute > 0) tempMinute - 1 else 59 }) {
                                Text("▼", fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("ANNULER", color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Button(
                            onClick = {
                                viewModel.updateHeure(tempHeure, tempMinute)
                                showTimePicker = false
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("VALIDER", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
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

            viewModel.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

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
                heureAffichee = heureAffichee,
                onHeureClick = { showTimePicker = true }
            )

            Text(
                text = "Sélectionnez les membres :",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (viewModel.listeAmis.isEmpty()) {
                    Text(
                        text = "Aucun autre utilisateur trouvé",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    viewModel.listeAmis.forEachIndexed { index, ami ->
                        AmiItem(
                            ami = ami,
                            onSelectionChange = { isChecked ->
                                viewModel.toggleAmiSelection(index, isChecked)
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.creerGroupe(
                            onSuccess = { onGroupeCree() }
                        )
                    },
                    enabled = !viewModel.isLoading,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("✓", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onAnnuler,
                    enabled = !viewModel.isLoading,
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
                text = "Heure du réveil",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clickable { onHeureClick() }
            ) {
                Text(
                    text = heureAffichee,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun AmiItem(
    ami: AmiUiModel,
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
            text = ami.pseudo,
            fontSize = 17.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NouveauGrpPreview() {
    ReveilTheme {
        NouveauGrp()
    }
}