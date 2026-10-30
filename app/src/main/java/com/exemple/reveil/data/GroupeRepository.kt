package com.exemple.reveil.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class GroupeData(
    val id: String = "",
    val nom: String = "",
    val mise: Int = 0,
    val heure: Int = 7,
    val minute: Int = 30,
    val membres: List<String> = emptyList()
)

class GroupeRepository {
    private val db = FirebaseFirestore.getInstance()
    private val groupesRef = db.collection("groupes")

    // Créer un groupe avec heure et minute personnalisées
    suspend fun creerGroupe(
        nom: String,
        mise: Int,
        membresUid: List<String>,
        heure: Int,
        minute: Int
    ): String {
        val nouveauDoc = groupesRef.document()
        val groupeData = mapOf(
            "id" to nouveauDoc.id,
            "nom" to nom,
            "mise" to mise,
            "membres" to membresUid,
            "heure" to heure,
            "minute" to minute
        )
        nouveauDoc.set(groupeData).await()
        return nouveauDoc.id
    }

    // Écouter un groupe spécifique
    fun ecouterGroupe(groupeId: String): Flow<GroupeData?> = callbackFlow {
        val subscription = groupesRef.document(groupeId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val groupe = snapshot?.toObject(GroupeData::class.java)
            trySend(groupe)
        }
        awaitClose { subscription.remove() }
    }

    // Écouter tous les groupes auxquels appartient le joueur
    fun ecouterGroupesDuJoueur(userUid: String): Flow<List<GroupeData>> = callbackFlow {
        val subscription = groupesRef
            .whereArrayContains("membres", userUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val groupes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(GroupeData::class.java)
                } ?: emptyList()
                trySend(groupes)
            }
        awaitClose { subscription.remove() }
    }

    // Modifier la mise d'un groupe
    suspend fun miser(groupeId: String, nouvelleMise: Int) {
        groupesRef.document(groupeId).update("mise", nouvelleMise).await()
    }

    // Modifier l'heure du réveil d'un groupe
    suspend fun modifierHeure(groupeId: String, heure: Int, minute: Int) {
        groupesRef.document(groupeId).update(
            mapOf("heure" to heure, "minute" to minute)
        ).await()
    }

    // Ajouter un membre au groupe
    suspend fun ajouterMembre(groupeId: String, nouveauMembreUid: String) {
        val doc = groupesRef.document(groupeId).get().await()
        val membresActuels = doc.get("membres") as? List<String> ?: emptyList()
        if (!membresActuels.contains(nouveauMembreUid)) {
            val nouveauxMembres = membresActuels + nouveauMembreUid
            groupesRef.document(groupeId).update("membres", nouveauxMembres).await()
        }
    }
}