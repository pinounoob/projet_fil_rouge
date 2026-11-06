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

@Composable
fun DetecteurSecousse(
    onFini: () -> Unit,
    modifier: Modifier = Modifier,
    objectif: Int = 10
) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometre = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    // si il n'y a pas d'accéléromètre on garde le bouton classique
    if (accelerometre == null) {
        Button(
            onClick = onFini,
            shape = RoundedCornerShape(50),
            modifier = modifier.fillMaxWidth().height(72.dp)
        ) { Text(stringResource(R.string.je_suis_debout), fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        return
    }

    var secousses by remember { mutableIntStateOf(0) }
    val onFiniActuel by rememberUpdatedState(onFini)   // la dernière version

    DisposableEffect(Unit) {
        var derniere = 0L
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                if (secousses >= objectif) return            // déjà fini on ne compte plus
                val (x, y, z) = e.values
                val force = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
                val maintenant = System.currentTimeMillis()
                if (force > 2.5f && maintenant - derniere > 300) {
                    derniere = maintenant
                    secousses++
                    if (secousses == objectif) onFiniActuel()  // appelé une seule fois
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, accelerometre, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensorManager.unregisterListener(listener) }   // libère le capteur
    }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.secoue_telephone), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { secousses / objectif.toFloat() },
            modifier = Modifier.fillMaxWidth().height(12.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text("$secousses / $objectif", fontSize = 18.sp)
    }
}