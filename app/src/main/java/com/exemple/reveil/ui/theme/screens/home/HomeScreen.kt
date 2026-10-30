package com.exemple.reveil.ui.theme.screens.home

import android.app.TimePickerDialog
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exemple.reveil.data.GroupeData
import com.exemple.reveil.ui.theme.screens.alarme.LignePodiumItem

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onNouveauGroupeClick: () -> Unit = {},
    onParametresClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val timePickerDialog = remember(viewModel.groupeSelectionne) {
        val h = viewModel.groupeSelectionne?.heure ?: 7
        val m = viewModel.groupeSelectionne?.minute ?: 30
        TimePickerDialog(
            context,
            { _, heurePicked, minutePicked ->
                viewModel.modifierHeure(heurePicked, minutePicked)
            },
            h,
            m,
            true
        )
    }

    if (viewModel.chargement) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Utilisateur
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Bonjour,", fontSize = 16.sp, color = Color.Gray)
                    Text(text = viewModel.nom, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Solde: ${viewModel.solde} €",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Sélection de groupe
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.changerMenuGroupe(true) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = viewModel.groupeSelectionne?.nom ?: "Sélectionner un groupe",
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "▼")
                    }
                }

                DropdownMenu(
                    expanded = viewModel.menuGroupeOuvert,
                    onDismissRequest = { viewModel.changerMenuGroupe(false) }
                ) {
                    viewModel.groupes.forEach { groupe ->
                        DropdownMenuItem(
                            text = { Text(groupe.nom) },
                            onClick = { viewModel.choisirGroupe(groupe) }
                        )
                    }
                }
            }

            // Infos du groupe
            viewModel.groupeSelectionne?.let { groupe ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Mise: ${viewModel.mise} €", fontSize = 16.sp)
                        Text(text = "Cagnotte: ${viewModel.cagnotte} €", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { timePickerDialog.show() }
                        ) {
                            Text(text = "Heure du réveil :", fontSize = 16.sp)
                            Text(text = viewModel.heureAlarme, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Boutons d'action
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
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

            // Podiums
            if (viewModel.classementsDuJour.isNotEmpty()) {
                Text(text = "Classement du jour", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.classementsDuJour) { item ->
                        LignePodiumItem(item = item)
                    }
                }
            }
        }
    }
}