package com.exemple.reveil.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

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

    //retire la mise du solde du joueur et l'ajoute à la cagnotte
    suspend fun miser(groupeId: String, uid: String) {
        val groupeRef = groupesRef.document(groupeId)
        val userRef = db.collection("users").document(uid)

        db.runTransaction { transaction ->
            //lire
            val groupe = transaction.get(groupeRef)
            val user = transaction.get(userRef)

            val mise = groupe.getLong("mise") ?: 0L
            val cagnotte = groupe.getLong("cagnotte") ?: 0L
            val solde = user.getLong("solde") ?: 0L

            //vérifier
            if (solde < mise) {
                throw FirebaseFirestoreException(
                    "Solde insuffisant",
                    FirebaseFirestoreException.Code.ABORTED
                )
            }

            //écrire
            transaction.update(userRef, "solde", solde - mise)
            transaction.update(groupeRef, "cagnotte", cagnotte + mise)
            null
        }.await()
    }
}