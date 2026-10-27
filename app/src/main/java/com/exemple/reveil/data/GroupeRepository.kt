package com.exemple.reveil.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class GroupeRepository {
    private val db = FirebaseFirestore.getInstance()
    private val groupesRef = db.collection("groupes")

    //ecoute un groupe en temps réel (nom, heure, cagnotte...)
    fun ecouterGroupe(groupeId: String): Flow<Groupe?> = callbackFlow {
        val subscription = groupesRef.document(groupeId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(Groupe::class.java))
        }
        awaitClose { subscription.remove() }
    }

    //ecoute en temps réel tous les groupes dont le joueur est membre
    fun ecouterGroupesDuJoueur(uid: String): Flow<List<Groupe>> = callbackFlow {
        val subscription = groupesRef
            .whereArrayContains("membres", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Groupe::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }
}