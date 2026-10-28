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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.material3.MaterialTheme
import com.exemple.reveil.data.Groupe
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import com.exemple.reveil.data.User

// Fonction la plus haute
@Composable
fun HomeScreen(modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onNouveauGroupe: () -> Unit = {},
    onParametres: () -> Unit = {}
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
            onAjouterGroupe = onNouveauGroupe,
            onLogoClick = onParametres
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
                    mise = groupe.mise,
                    solde = viewModel.solde,
                    onAjouter = { viewModel.ajouterCagnotte() }
                )

                viewModel.erreur?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

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

                if (viewModel.popupContactOuvert) {
                    PopupAjoutContact(
                        recherche = viewModel.rechercheContact,
                        joueurs = viewModel.joueursProposes,
                        onRechercheChange = { viewModel.changerRechercheContact(it) },
                        onJoueurChoisi = { viewModel.choisirContact(it) },
                        onFermer = { viewModel.fermerPopupContact() }
                    )
                }
            }
        }
    }
}

// Fonctions de bas niveau
@Composable
fun BarreDuHaut(
    recherche: String,
    onRechercheChange: (String) -> Unit,
    onAjouterGroupe: () -> Unit,
    onLogoClick: () -> Unit
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
                .clickable{onLogoClick()}
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

// Petit titre gris + petit bouton +
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
fun Cagnotte(montant: Int, mise: Int, solde: Int, onAjouter: () -> Unit) {
    Column(horizontalAlignment = Alignment.Start) {
        TitreAvecPlus(titre = "Cagnotte (+$mise € par mise)", onAjouter = onAjouter)
        Text(text = "$montant €", fontSize = 48.sp, fontWeight = FontWeight.Bold)
        Text(text = "Ton solde : $solde €", fontSize = 14.sp, color = Color.Gray)
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

//classement du matin + contacts, dans une seule liste qui défile
@Composable
fun ListesAccueil(
    classement: List<LignePodium>,
    contacts: List<String>,
    onAjouterContact: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        //classement du matin
        item {
            Text(text = "Classement du matin", fontSize = 14.sp, color = Color.Gray)
        }
        if (classement.isEmpty()) {
            item {
                Text(text = "Personne ne s'est encore levé ce matin.", fontSize = 16.sp)
            }
        } else {
            items(classement) { ligne ->
                LignePodiumItem(ligne = ligne)
            }
        }

        //contacts
        item {
            Column {
                Spacer(modifier = Modifier.size(12.dp))
                TitreAvecPlus(titre = "Contacts", onAjouter = onAjouterContact)
            }
        }
        if (contacts.isEmpty()) {
            item {
                Text(
                    text = "Aucun contact pour l'instant. Appuie sur + pour en ajouter.",
                    fontSize = 16.sp
                )
            }
        } else {
            items(contacts) { contact ->
                Text(text = contact, fontSize = 18.sp)
            }
        }
    }
}

    //popup pour chercher un joueur et l'ajouter au groupe
    @Composable
    fun PopupAjoutContact(
        recherche: String,
        joueurs: List<User>,
        onRechercheChange: (String) -> Unit,
        onJoueurChoisi: (User) -> Unit,
        onFermer: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onFermer,
            title = { Text("Ajouter un contact") },
            text = {
                Column {
                    OutlinedTextField(
                        value = recherche,
                        onValueChange = onRechercheChange,
                        placeholder = { Text("Pseudo du joueur") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.size(12.dp))

                    when {
                        recherche.isBlank() ->
                            Text("Tape un pseudo pour chercher.", color = Color.Gray)
                        joueurs.isEmpty() ->
                            Text("Aucun joueur trouvé.", color = Color.Gray)
                        else ->
                            LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                                items(joueurs) { joueur ->
                                    Text(
                                        text = joueur.pseudo,
                                        fontSize = 18.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onJoueurChoisi(joueur) }
                                            .padding(vertical = 12.dp)
                                    )
                                }
                            }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onFermer) { Text("Annuler") }
            }
        )
    }


// Cas oublié : l'utilisateur n'a encore aucun groupe
@Composable
fun AucunGroupe(
    onCreerGroupe: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "😴", fontSize = 64.sp)
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = "Tu n'as encore aucun groupe",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Crée un groupe et invite tes amis pour lancer la course du matin.",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.size(24.dp))
        Button(onClick = onCreerGroupe) {
            Text("Créer un groupe")
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

@Preview(showBackground = true)
@Composable
fun PopupAjoutContactPreview() {
    ReveilTheme {
        PopupAjoutContact(
            recherche = "al",
            joueurs = listOf(User(uid = "1", pseudo = "Aliona"), User(uid = "2", pseudo = "Alex")),
            onRechercheChange = {},
            onJoueurChoisi = {},
            onFermer = {}
        )
    }
}