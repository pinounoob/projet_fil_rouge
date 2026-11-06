package com.exemple.reveil.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.exemple.reveil.util.AlarmeScheduler
import com.exemple.reveil.util.NotificationAlarme

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val groupeId = intent.getStringExtra("groupeId") ?: return

        // Affiche la notification avec le son d'alarme (K2)
        NotificationAlarme.afficher(context, groupeId)

        // Reprogramme automatiquement l'alarme pour demain (K3)
        val heure = intent.getIntExtra("heure", -1)
        val minute = intent.getIntExtra("minute", -1)
        if (heure >= 0 && minute >= 0) {
            AlarmeScheduler.programmerAlarme(context, groupeId, heure, minute)
        }
    }
}