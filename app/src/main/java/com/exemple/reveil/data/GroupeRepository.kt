package com.exemple.reveil.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.exemple.reveil.logique.calculerGains
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

            //vérifier : une seule mise par personne et par réveil
            val dejaMise = (groupe.get("misesDuTour") as? List<*>)?.contains(uid) == true
            if (dejaMise) {
                throw FirebaseFirestoreException(
                    "Déjà misé",
                    FirebaseFirestoreException.Code.ALREADY_EXISTS
                )
            }
            if (solde < mise) {
                throw FirebaseFirestoreException(
                    "Solde insuffisant",
                    FirebaseFirestoreException.Code.ABORTED
                )
            }

            //écrire
            transaction.update(userRef, "solde", solde - mise)
            transaction.update(
                groupeRef,
                mapOf(
                    "cagnotte" to cagnotte + mise,
                    "misesDuTour" to FieldValue.arrayUnion(uid)   // il participe au prochain réveil
                )
            )
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

    //distribue la cagnotte une seule fois par matin, entre ceux qui ont misé,
    //dès que tous sont levés sauf un (le dernier ne gagne rien)
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

        db.runTransaction { transaction ->
            //lire
            val matin = transaction.get(matinRef)
            if (matin.getBoolean("distribue") == true) return@runTransaction null   // déjà fait ce matin

            val groupe = transaction.get(groupeRef)
            if (!groupe.exists()) return@runTransaction null

            //seuls ceux qui ont misé participent
            val participants = (groupe.get("misesDuTour") as? List<*>)
                ?.mapNotNull { it as? String } ?: emptyList()
            if (participants.isEmpty()) return@runTransaction null

            //les participants levés, dans l'ordre
            val leves = resultats.map { it.uid }.filter { it in participants }.distinct()
            //il faut au moins un levé, et tout le monde levé sauf au plus un :
            //celui qui reste est forcément le dernier
            if (leves.isEmpty() || leves.size < participants.size - 1) return@runTransaction null

            val classement = leves + participants.filter { it !in leves }
            val cagnotte = (groupe.getLong("cagnotte") ?: 0L).toInt()
            val calcul = calculerGains(classement, cagnotte)

            //écrire
            calcul.gains.filter { it.montant > 0 }.forEach { gain ->
                transaction.set(
                    db.collection("users").document(gain.joueur),
                    mapOf("solde" to FieldValue.increment(gain.montant.toLong())),
                    SetOptions.merge()
                )
            }
            //cagnotte remise à jour et mises remises à zéro : chacun pourra remiser
            transaction.update(
                groupeRef,
                mapOf(
                    "cagnotte" to calcul.cagnotteRestante.toLong(),
                    "misesDuTour" to emptyList<String>()
                )
            )
            transaction.set(
                matinRef,
                Matin(distribue = true, cagnotteAvant = cagnotte, participants = participants)
            )
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
                // Il reste d'autres membres : retrait du membre, et de la liste des mises
                // (sa mise reste dans la cagnotte, mais il ne bloque plus la distribution)
                transaction.update(
                    ref,
                    mapOf(
                        "membres" to FieldValue.arrayRemove(uid),
                        "misesDuTour" to FieldValue.arrayRemove(uid)
                    )
                )
            }
            null
        }.await()
    }

    // active ou désactive l'alarme du groupe
    suspend fun modifierAlarmeActive(groupeId: String, active: Boolean) {
        groupesRef.document(groupeId).update("alarmeActive", active).await()
    }
}