package com.exemple.reveil.ui.theme.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exemple.reveil.R
import com.exemple.reveil.ui.theme.ReveilTheme
import androidx.compose.foundation.clickable
import com.exemple.reveil.ui.theme.screens.alarme.LignePodium
import com.exemple.reveil.ui.theme.screens.alarme.LignePodiumItem
import androidx.compose.material3.CircularProgressIndicator
import com.exemple.reveil.data.Groupe

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onNouveauGroupe: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BarreDuHaut(
            recherche = viewModel.recherche,
            onRechercheChange = { viewModel.changerRecherche(it) },
            onAjouterGroupe = onNouveauGroupe
        )

        val groupe = viewModel.groupeSelectionne

        when {
            viewModel.chargement -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            groupe == null -> {
                //cas oublié : aucun groupe
                AucunGroupe(
                    onCreerGroupe = onNouveauGroupe,
                    modifier = Modifier.weight(1f)
                )
            }
            else -> {
                Spacer(modifier = Modifier.size(40.dp))

                SelecteurGroupe(
                    groupeSelectionne = groupe.nom,
                    groupes = viewModel.groupes,
                    menuOuvert = viewModel.menuGroupeOuvert,
                    onMenuOuvertChange = { viewModel.changerMenuGroupe(it) },
                    onGroupeChoisi = { viewModel.choisirGroupe(it) },
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.size(30.dp))

                Cagnotte(
                    montant = groupe.cagnotte,
                    onAjouter = { viewModel.ajouterCagnotte() }
                )

                Spacer(modifier = Modifier.size(40.dp))

                Alarme(
                    minutes = groupe.heureAlarme,
                    onAjouter = { viewModel.ajouterAlarme() }
                )

                Spacer(modifier = Modifier.size(32.dp))

                ListesAccueil(
                    classement = viewModel.classementDuJour,
                    contacts = viewModel.contacts,
                    onAjouterContact = { viewModel.ajouterContact() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun BarreDuHaut(
    recherche: String,
    onRechercheChange: (String) -> Unit,
    onAjouterGroupe: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_reveil),
            contentDescription = "Logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.size(8.dp))

        TextField(
            value = recherche,
            onValueChange = onRechercheChange,
            placeholder = { Text("Rechercher") },
            singleLine = true,
            shape = RoundedCornerShape(50),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.size(8.dp))

        FilledIconButton(onClick = onAjouterGroupe) {
            Text("+", fontSize = 24.sp)
        }
    }
}

@Composable
fun SelecteurGroupe(
    groupeSelectionne: String,
    groupes: List<Groupe>,
    menuOuvert: Boolean,
    onMenuOuvertChange: (Boolean) -> Unit,
    onGroupeChoisi: (Groupe) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Button(onClick = { onMenuOuvertChange(true) }) {
            Text("$groupeSelectionne ▾")
        }
        DropdownMenu(
            expanded = menuOuvert,
            onDismissRequest = { onMenuOuvertChange(false) }
        ) {
            groupes.forEach { groupe ->
                DropdownMenuItem(
                    text = { Text(groupe.nom) },
                    onClick = { onGroupeChoisi(groupe) }
                )
            }
        }
    }
}

@Composable
fun TitreAvecPlus(titre: String, onAjouter: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = titre, fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.size(6.dp))
        FilledIconButton(onClick = onAjouter, modifier = Modifier.size(20.dp)) {
            Text("+", fontSize = 12.sp)
        }
    }
}

@Composable
fun Cagnotte(montant: Int, onAjouter: () -> Unit) {
    Column(horizontalAlignment = Alignment.Start) {
        TitreAvecPlus(titre = "Cagnotte", onAjouter = onAjouter)
        Text(text = "$montant €", fontSize = 48.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Alarme(minutes: Int, onAjouter: () -> Unit) {
    val heureTexte = "%02d:%02d".format(minutes / 60, minutes % 60)
    Column(horizontalAlignment = Alignment.Start) {
        TitreAvecPlus(titre = "Alarme", onAjouter = onAjouter)
        Text(text = "⏰ $heureTexte", fontSize = 36.sp)
    }
}

@Composable
fun Contacts(contacts: List<String>, onAjouter: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        TitreAvecPlus(titre = "Contacts", onAjouter = onAjouter)
        Spacer(modifier = Modifier.size(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            contacts.forEach { contact ->
                Text(contact, fontSize = 18.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AucunGroupePreview() {
    ReveilTheme {
        AucunGroupe(onCreerGroupe = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ListesAccueilVidesPreview() {
    ReveilTheme {
        ListesAccueil(classement = emptyList(), contacts = emptyList(), onAjouterContact = {})
    }
}