package com.exemple.reveil.ui.components

import com.exemple.reveil.R
import androidx.compose.ui.res.stringResource
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt

// Pour arrêter l'alarme, il faut secouer le téléphone
// Quand c'est fait, on appelle onFini (l'écran d'alarme passe alors au podium)
@Composable
fun DetecteurSecousse(
    onFini: () -> Unit,          // action à lancer quand on a assez secoué
    modifier: Modifier = Modifier,
    objectif: Int = 10           // nombre de secousses demandées
) {
    // Le Context donne accès aux services du système, dont le gestionnaire de capteurs
    val context = LocalContext.current
    // On récupère le gestionnaire et le capteur une seule fois
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    // l'accéléromètre du téléphone, ou null si le téléphone n'en a pas
    val accelerometre = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    // s'il n'y a pas d'accéléromètre, on garde le bouton classique
    if (accelerometre == null) {
        Button(
            onClick = onFini,
            shape = RoundedCornerShape(50),
            modifier = modifier.fillMaxWidth().height(72.dp)
        ) { Text(stringResource(R.string.je_suis_debout), fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        return
    }

    // nombre de secousses déjà faites (chaque changement redessine la barre)
    var secousses by remember { mutableIntStateOf(0) }
    // on garde toujours la dernière version de onFini
    val onFiniActuel by rememberUpdatedState(onFini)

    DisposableEffect(Unit) {
        var derniere = 0L   // heure (en ms) de la dernière secousse comptée

        // le listener reçoit les mesures de l'accéléromètre
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                if (secousses >= objectif) return            // déjà fini, on ne compte plus

                // accélération sur les 3 axes, en m/s²
                val (x, y, z) = e.values
                // force totale du mouvement, en "g" (posé sur la table environ 1 g)
                val force = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
                val maintenant = System.currentTimeMillis()

                // une secousse = mouvement fort (> 2,5 g) ET au moins 300 ms depuis la précédente
                // sinon un seul geste serait compté plusieurs fois
                if (force > 2.5f && maintenant - derniere > 300) {
                    derniere = maintenant
                    secousses++
                    if (secousses == objectif) onFiniActuel()  // appelé une seule fois
                }
            }

            // obligatoire dans l'interface
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        // on commence à écouter le capteur (SENSOR_DELAY_GAME : mises à jour rapides)
        sensorManager.registerListener(listener, accelerometre, SensorManager.SENSOR_DELAY_GAME)

        // quand l'écran disparaît : on arrête le capteur pour ne pas vider la batterie
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // consigne + barre de progression + compteur
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.secoue_telephone), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        // progression de 0 à 1, toFloat() sinon 4 / 10 = 0 (division entière)
        LinearProgressIndicator(
            progress = { secousses / objectif.toFloat() },
            modifier = Modifier.fillMaxWidth().height(12.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text("$secousses / $objectif", fontSize = 18.sp)
    }
}