package com.exemple.reveil.ui.screens.alarme

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exemple.reveil.data.PreferenceManager
import com.exemple.reveil.ui.components.DetecteurSecousse
import com.exemple.reveil.ui.theme.ReveilTheme

// Fonction la plus haute
@Composable
fun AlarmeScreen(
    groupeId: String,
    modifier: Modifier = Modifier,
    viewModel: AlarmeViewModel = viewModel(),
    onRetourAccueil: () -> Unit = {}
) {
    // Exécuté à l'ouverture de l'écran (et si groupeId change)
    LaunchedEffect(groupeId) {
        viewModel.chargerGroupe(groupeId)
    }

    val context = LocalContext.current
    val preferenceManager = remember(context) { PreferenceManager(context.applicationContext) }
    val ringtoneMode by preferenceManager.ringtoneModeFlow.collectAsState(initial = "SONNERIE")
    val ringtoneUriString by preferenceManager.ringtoneUriFlow.collectAsState(initial = null)

    // Son de l'alarme ou vibreur : sonne ou vibre en boucle tant qu'on n'a pas appuyé sur « Je suis debout »
    DisposableEffect(viewModel.estDebout, ringtoneMode, ringtoneUriString) {
        var mediaPlayer: MediaPlayer? = null
        var vibrator: Vibrator? = null

        if (!viewModel.estDebout) {
            if (ringtoneMode == "VIBRATION") {
                // Mode Vibra : déclenchement du vibreur en boucle
                vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }

                val timings = longArrayOf(0, 800, 400) // Vibre 800ms, pause 400ms
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(timings, 0))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(timings, 0)
                }
            } else {
                // Mode Sonnerie : lecture du son choisi ou son d'alarme par défaut
                val targetUri = ringtoneUriString?.let { Uri.parse(it) }
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                try {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(context, targetUri)
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        isLooping = true
                        prepare()
                        start()
                    }
                } catch (e: Exception) {
                    val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    mediaPlayer = MediaPlayer.create(context, defaultUri)?.apply {
                        isLooping = true
                        start()
                    }
                }
            }
        }
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            vibrator?.cancel()
        }
    }

    if (viewModel.estDebout) {
        Podium(
            podium = viewModel.podium,
            cagnotte = viewModel.cagnotteApresGains,
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
        DetecteurSecousse(onFini = onJeSuisDebout)
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
fun AlarmeEnCoursPreview() {
    ReveilTheme {
        AlarmeEnCours(
            heureAlarme = 7 * 60,
            nomGroupe = "Groupe test",
            cagnotte = 40,
            onJeSuisDebout = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PodiumPreview() {
    ReveilTheme {
        Podium(
            podium = listOf(
                LignePodium(
                    rang = 1,
                    nom = "Toi",
                    gain = 5
                ),
                LignePodium(
                    rang = 2,
                    nom = "Aliona",
                    gain = 3
                )
            ),
            cagnotte = 142,
            onRetourAccueil = {}
        )
    }
}
