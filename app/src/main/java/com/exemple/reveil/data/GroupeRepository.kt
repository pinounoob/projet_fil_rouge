package com.exemple.reveil.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.exemple.reveil.logique.GAINS_PAR_RANG
import com.google.firebase.firestore.SetOptions

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
        val groupe = Groupe(
            nom = nom,
            heureAlarme = heure * 60 + minute,
            alarmeActive = true,
            mise = mise,
            cagnotte = 0,
            membres = membresUid.distinct()
        )
        nouveauDoc.set(groupe).await()
        return nouveauDoc.id
    }

    //ecoute un groupe en temps réel
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

    //ajoute un joueur au groupe (sans doublon, sans écraser les autres membres)
    suspend fun ajouterMembre(groupeId: String, uid: String) {
        groupesRef.document(groupeId)
            .update("membres", FieldValue.arrayUnion(uid))
            .await()
    }

    private fun matinRef(groupeId: String, jour: String) =
        groupesRef.document(groupeId).collection("matins").document(jour)

    //écoute le matin du jour (pour savoir si les gains sont déjà distribués)
    fun ecouterMatin(groupeId: String, jour: String): Flow<Matin?> = callbackFlow {
        val subscription = matinRef(groupeId, jour).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(Matin::class.java))
        }
        awaitClose { subscription.remove() }
    }

    //distribue les gains du matin au fur et à mesure que les joueurs se réveillent
    suspend fun distribuerGains(groupeId: String, jour: String) {
        val groupeRef = groupesRef.document(groupeId)
        val matinRef = matinRef(groupeId, jour)

        //les réveils du jour du plus tôt au plus tard
        val resultats = groupeRef.collection("resultats")
            .whereEqualTo("jour", jour)
            .get().await()
            .toObjects(Resultat::class.java)
            .filter { it.heureArret != null }
            .sortedBy { it.heureArret }

        if (resultats.isEmpty()) return

        db.runTransaction { transaction ->
            val matinSnap = transaction.get(matinRef)
            val groupeSnap = transaction.get(groupeRef)

            if (!groupeSnap.exists()) return@runTransaction null

            var cagnotteActuelle = (groupeSnap.getLong("cagnotte") ?: 0L).toInt()
            val cagnotteInitiale = matinSnap.getLong("cagnotteAvant")?.toInt() ?: cagnotteActuelle

            val joueursTraites = (matinSnap.get("joueursTraites") as? List<*>)
                ?.mapNotNull { it?.toString() }
                ?.toMutableList() ?: mutableListOf()

            var modifie = false

            resultats.forEachIndexed { index, resultat ->
                val uid = resultat.uid
                if (uid.isNotBlank() && uid !in joueursTraites) {
                    val prevu = GAINS_PAR_RANG.getOrElse(index) { 0 }
                    val gainReel = minOf(prevu, cagnotteActuelle)

                    if (gainReel > 0) {
                        val userRef = db.collection("users").document(uid)
                        transaction.set(
                            userRef,
                            mapOf("solde" to FieldValue.increment(gainReel.toLong())),
                            SetOptions.merge()
                        )
                        cagnotteActuelle -= gainReel
                    }
                    joueursTraites.add(uid)
                    modifie = true
                }
            }

            if (modifie) {
                transaction.update(groupeRef, "cagnotte", cagnotteActuelle.toLong())
                transaction.set(
                    matinRef,
                    mapOf(
                        "distribue" to true,
                        "cagnotteAvant" to cagnotteInitiale,
                        "joueursTraites" to joueursTraites
                    ),
                    SetOptions.merge()
                )
            }
            null
        }.await()
    }

    //change l'heure de l'alarme du groupe
    suspend fun modifierHeure(groupeId: String, minutes: Int) {
        require(minutes in 0 until 24 * 60) { "Heure invalide" }
        groupesRef.document(groupeId).update("heureAlarme", minutes).await()
    }

    //retire un joueur du groupe ; si la liste des membres devient vide, le groupe est automatiquement supprimé de Firestore
    suspend fun retirerMembre(groupeId: String, uid: String) {
        val ref = groupesRef.document(groupeId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            if (!snapshot.exists()) return@runTransaction null

            val membres = (snapshot.get("membres") as? List<*>) ?: emptyList<Any>()
            val membresRestants = membres.filter { it != uid }

            if (membresRestants.isEmpty()) {
                // Plus aucun membre : suppression définitive du document groupe dans Firestore
                transaction.delete(ref)
            } else {
                // Il reste d'autres membres : retrait du membre uniquement
                transaction.update(ref, "membres", FieldValue.arrayRemove(uid))
            }
            null
        }.await()
    }

    // active ou désactive l'alarme du groupe
    suspend fun modifierAlarmeActive(groupeId: String, active: Boolean) {
        groupesRef.document(groupeId).update("alarmeActive", active).await()
    }
}