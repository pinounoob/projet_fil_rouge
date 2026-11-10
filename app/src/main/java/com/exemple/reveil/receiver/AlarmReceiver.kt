package com.exemple.reveil.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.exemple.reveil.data.Groupe
import com.exemple.reveil.util.AlarmeScheduler
import com.exemple.reveil.util.NotificationAlarme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val groupeId = intent.getStringExtra("groupeId") ?: return
        val heurePrevue = intent.getIntExtra("heure", -1)
        val minutePrevue = intent.getIntExtra("minute", -1)
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        // Sonne avec l'heure de l'Intent (si on ne peut pas vérifier Firestore)
        fun sonnerEtReprogrammer() {
            NotificationAlarme.afficher(context, groupeId)
            if (heurePrevue >= 0 && minutePrevue >= 0) {
                AlarmeScheduler.programmerAlarme(context, groupeId, heurePrevue, minutePrevue)
            }
        }

        if (uid == null) { sonnerEtReprogrammer(); return }

        // goAsync : laisse le temps de lire Firestore (onReceive doit finir vite)
        val enCours = goAsync()
        FirebaseFirestore.getInstance().collection("groupes").document(groupeId).get()
            .addOnSuccessListener { doc ->
                val groupe = doc.toObject(Groupe::class.java)

                // Groupe supprimé, alarme coupée ou plus membre : on ne sonne pas
                if (groupe == null || !groupe.alarmeActive || uid !in groupe.membres) {
                    AlarmeScheduler.annulerAlarme(context, groupeId)
                    return@addOnSuccessListener
                }

                val heure = groupe.heureAlarme / 60
                val minute = groupe.heureAlarme % 60

                if (heure == heurePrevue && minute == minutePrevue) {
                    // Bonne heure : on sonne, puis on reprogramme pour demain
                    NotificationAlarme.afficher(context, groupeId)
                }
                // Dans tous les cas, on (re)programme avec l'heure à jour
                AlarmeScheduler.programmerAlarme(context, groupeId, heure, minute)
            }
            .addOnFailureListener { e ->
                Log.e("AlarmReceiver", "Firestore injoignable, on sonne quand même", e)
                sonnerEtReprogrammer()
            }
            .addOnCompleteListener { enCours.finish() }
    }
}