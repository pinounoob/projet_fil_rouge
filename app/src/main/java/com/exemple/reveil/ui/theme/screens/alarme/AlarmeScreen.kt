package com.exemple.reveil.ui.theme.screens.alarme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exemple.reveil.ui.theme.ReveilTheme

// Fonction la plus haute
@Composable
fun AlarmeScreen(
    groupeId: Int,
    modifier: Modifier = Modifier,
    viewModel: AlarmeViewModel = viewModel(),
    onRetourAccueil: () -> Unit = {}
) {
    // Exécuté à l'ouverture de l'écran (et si groupeId change)
    LaunchedEffect(groupeId) {
        viewModel.chargerGroupe(groupeId)
    }

    if (viewModel.estDebout) {
        Podium(
            podium = viewModel.podium,
            cagnotte = viewModel.cagnotte,
            onRetourAccueil = onRetourAccueil,
            modifier = modifier
        )
    } else {
        AlarmeEnCours(
            heureAlarme = viewModel.heureAlarme,
            nomGroupe = viewModel.nomGroupe,
            cagnotte = viewModel.cagnotte,
            onJeSuisDebout = { viewModel.jeSuisDebout() },
            modifier = modifier
        )
    }
}

// Fonctions de bas niveau

@Composable
fun AlarmeEnCours(
    heureAlarme: Int,
    nomGroupe: String,
    cagnotte: Int,
    onJeSuisDebout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heureTexte = "%02d:%02d".format(heureAlarme / 60, heureAlarme % 60)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(text = "⏰", fontSize = 64.sp)
        Text(text = heureTexte, fontSize = 80.sp, fontWeight = FontWeight.Bold)
        Text(text = nomGroupe, fontSize = 24.sp)
        Spacer(modifier = Modifier.size(16.dp))
        Text(text = "Cagnotte : $cagnotte €", fontSize = 20.sp, color = Color.Gray)

        Spacer(modifier = Modifier.weight(1f))

        // bouton en bas
        Button(
            onClick = onJeSuisDebout,
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            Text("Je suis debout !", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun Podium(
    podium: List<LignePodium>,
    cagnotte: Int,
    onRetourAccueil: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Classement du matin", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.size(24.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(podium) { ligne ->
                LignePodiumItem(ligne = ligne)
            }
        }

        Text(text = "Reste dans la cagnotte : $cagnotte €", fontSize = 18.sp, color = Color.Gray)
        Spacer(modifier = Modifier.size(16.dp))

        Button(
            onClick = onRetourAccueil,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Retour à l'accueil", fontSize = 18.sp)
        }
    }
}

@Composable
fun LignePodiumItem(ligne: LignePodium) {
    val medaille = when (ligne.rang) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "😴"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = medaille, fontSize = 28.sp)
        Spacer(modifier = Modifier.size(12.dp))
        Text(text = ligne.nom, fontSize = 20.sp, modifier = Modifier.weight(1f))
        Text(text = "+${ligne.gain} €", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AlarmeScreenPreview() {
    ReveilTheme {
        AlarmeScreen(groupeId = 1)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PodiumPreview() {
    ReveilTheme {
        Podium(
            podium = listOf(
                LignePodium(rang = 1, nom = "Toi", gain = 5),
                LignePodium(rang = 2, nom = "Aliona", gain = 3)
            ),
            cagnotte = 142,
            onRetourAccueil = {}
        )
    }
}