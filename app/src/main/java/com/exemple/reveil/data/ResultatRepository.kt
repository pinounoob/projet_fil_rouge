package com.exemple.reveil.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ResultatRepository {
    private val db = FirebaseFirestore.getInstance()

    //groupes/{groupeId}/resultats
    private fun resultatsRef(groupeId: String) =
        db.collection("groupes").document(groupeId).collection("resultats")

    //enregistre le réveil d'un joueur
    suspend fun envoyerResultat(groupeId: String, uid: String, pseudo: String, jour: String) {
        val resultat = Resultat(uid = uid, pseudo = pseudo, jour = jour)
        resultatsRef(groupeId)
            .document("${jour}_$uid")
            .set(resultat)
            .await()
    }

    //ecoute en temps réel les réveils du jour dans un groupe
    fun ecouterResultatsDuJour(groupeId: String, jour: String): Flow<List<Resultat>> = callbackFlow {
        val subscription = resultatsRef(groupeId)
            .whereEqualTo("jour", jour)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val liste = snapshot.toObjects(
                        Resultat::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    trySend(liste)
                }
            }
        awaitClose { subscription.remove() }
    }
}