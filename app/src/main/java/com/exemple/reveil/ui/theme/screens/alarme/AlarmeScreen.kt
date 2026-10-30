package com.exemple.reveil.ui.theme.screens.alarme

import android.media.MediaPlayer
import android.media.RingtoneManager
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// Data class pour chaque ligne du classement podium
data class LignePodium(
    val rang: Int,
    val nom: String,
    val gain: Int
)

@Composable
fun AlarmeScreen(
    groupeId: String,
    modifier: Modifier = Modifier,
    viewModel: AlarmeViewModel = viewModel(),
    onRetourAccueil: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(groupeId) {
        viewModel.chargerGroupe(groupeId)
    }

    // GESTION DU SON D'ALARME SYSTEME (Utilise le son par défaut de la tablette)
    DisposableEffect(uiState.estReveille, uiState.isLoading) {
        var mediaPlayer: MediaPlayer? = null

        if (!uiState.estReveille && !uiState.isLoading) {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer.create(context, alarmUri)?.apply {
                isLooping = true // Joue en boucle
                start()
            }
        }

        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.estReveille) {
            PodiumContent(
                podium = listOf(
                    LignePodium(1, "Toi", 5),
                    LignePodium(2, "Aliona", 3)
                ),
                cagnotteApresGains = uiState.cagnotteTotale,
                onRetourAccueil = onRetourAccueil
            )
        } else {
            AlarmeEnCoursContent(
                heureAlarme = uiState.heureFormatee,
                nomGroupe = uiState.groupe?.nom ?: "Groupe",
                cagnotte = uiState.cagnotteTotale,
                onJeSuisDebout = { viewModel.validerReveil() }
            )
        }
    }
}

@Composable
fun AlarmeEnCoursContent(
    heureAlarme: String,
    nomGroupe: String,
    cagnotte: Int,
    onJeSuisDebout: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = nomGroupe,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = heureAlarme,
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Cagnotte en jeu : $cagnotte €",
            fontSize = 18.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onJeSuisDebout,
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Je suis réveillé !", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PodiumContent(
    podium: List<LignePodium>,
    cagnotteApresGains: Int,
    onRetourAccueil: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Classement du matin",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(podium) { item ->
                LignePodiumItem(item)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Reste dans la cagnotte : $cagnotteApresGains €",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Button(
            onClick = onRetourAccueil,
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Retour à l'accueil")
        }
    }
}

@Composable
fun LignePodiumItem(item: LignePodium) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (item.rang) {
                        1 -> "🥇"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> "${item.rang}."
                    },
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = item.nom, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = "+${item.gain} €",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}