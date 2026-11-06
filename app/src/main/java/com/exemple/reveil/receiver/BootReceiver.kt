package com.exemple.reveil.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.exemple.reveil.data.Groupe
import com.exemple.reveil.util.AlarmeScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// au redémarrage du téléphone, les alarmes sont perdues, on relit les groupes
class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val enCours = goAsync() // laisse le temps de lire Firestore

        FirebaseFirestore.getInstance().collection("groupes")
            .whereArrayContains("membres", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.toObjects(Groupe::class.java)
                    .filter { it.alarmeActive } // On ne relance que les alarmes actives
                    .forEach { groupe ->
                        AlarmeScheduler.programmerAlarme(
                            context,
                            groupe.id,
                            groupe.heureAlarme / 60, // Les heures
                            groupe.heureAlarme % 60  // Les minutes (ligne corrigée)
                        )
                    }
            }
            .addOnCompleteListener { enCours.finish() }
    }
}