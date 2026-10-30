package com.exemple.reveil.ui.theme.screens.home

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exemple.reveil.data.GroupeData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onNouveauGroupeClick: () -> Unit = {},
    onParametresClick: () -> Unit = {}
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val groupeActuel = viewModel.groupeSelectionne

    // Pop-up d'édition de l'heure
    if (showTimePicker && groupeActuel != null) {
        var tempHeure by remember { mutableIntStateOf(groupeActuel.heure) }
        var tempMinute by remember { mutableIntStateOf(groupeActuel.minute) }

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
                        text = "Modifier l'heure du réveil",
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
                                viewModel.modifierHeureGroupe(tempHeure, tempMinute)
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
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sélection de groupe
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = groupeActuel?.nom ?: "Sélectionner un groupe",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    viewModel.groupes.forEach { groupe ->
                        DropdownMenuItem(
                            text = { Text(groupe.nom) },
                            onClick = {
                                viewModel.melectionnerGroupe(groupe)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Détails du groupe et heure cliquable
            groupeActuel?.let { groupe ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Mise : ${groupe.mise} €", fontSize = 16.sp)
                        Text(text = "Cagnotte : ${groupe.mise * groupe.membres.size} €", fontSize = 16.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Heure du réveil :", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                            val heureFormatee = "%02d:%02d".format(groupe.heure, groupe.minute)

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.clickable { showTimePicker = true }
                            ) {
                                Text(
                                    text = heureFormatee,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Boutons du bas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onNouveauGroupeClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Nouveau groupe")
                }

                Button(
                    onClick = onParametresClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Paramètres")
                }
            }
        }
    }
}