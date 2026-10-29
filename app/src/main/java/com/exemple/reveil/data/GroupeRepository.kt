package com.exemple.reveil.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.exemple.reveil.GAINS_PAR_RANG
import com.exemple.reveil.calculerGains
import com.google.firebase.firestore.SetOptions

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

    //distribue les gains du matin une seule fois par groupe et par jour
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
            if (matin.getBoolean("distribue") == true) return@runTransaction null

            val groupe = transaction.get(groupeRef)
            val nbMembres = (groupe.get("membres") as? List<*>)?.size ?: 0
            val nbGagnants = minOf(GAINS_PAR_RANG.size, nbMembres)
            if (nbGagnants == 0 || resultats.size < nbGagnants) return@runTransaction null   // pas encore assez de levés

            //calculer avec la cagnotte à jour
            val cagnotte = (groupe.getLong("cagnotte") ?: 0L).toInt()
            val calcul = calculerGains(resultats.map { it.uid }, cagnotte)

            //écrire
            calcul.gains.filter { it.montant > 0 }.forEach { gain ->
                val userRef = db.collection("users").document(gain.joueur)
                transaction.set(
                    userRef,
                    mapOf("solde" to FieldValue.increment(gain.montant.toLong())),
                    SetOptions.merge()
                )
            }
            transaction.update(groupeRef, "cagnotte", calcul.cagnotteRestante.toLong())
            transaction.set(matinRef, Matin(distribue = true, cagnotteAvant = cagnotte))
            null
        }.await()
    }

    //change l'heure de l'alarme du groupe
    suspend fun modifierHeure(groupeId: String, minutes: Int) {
        require(minutes in 0 until 24 * 60) { "Heure invalide" }
        groupesRef.document(groupeId).update("heureAlarme", minutes).await()
    }
}