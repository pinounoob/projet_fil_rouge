package com.exemple.reveil.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")

    suspend fun addUser(user: User) {
        usersRef.document(user.uid).set(user).await()
    }

    suspend fun pseudoPris(pseudo: String, excepteUid: String? = null): Boolean {
        val snapshot = usersRef.get().await()
        val targetPseudo = pseudo.trim()
        return snapshot.documents.any { doc ->
            val docUid = doc.id
            val docPseudo = doc.getString("pseudo")
            docUid != excepteUid && docPseudo?.equals(targetPseudo, ignoreCase = true) == true
        }
    }

    suspend fun updateUser(uid: String, nouveauPseudo: String) {
        usersRef.document(uid).update("pseudo", nouveauPseudo).await()
    }

    suspend fun getUser(uid: String): User? =
        usersRef.document(uid).get().await().toObject(User::class.java)

    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val subscription = usersRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                // Convertit les documents Firestore en objets User
                val listeUsers = snapshot.toObjects(User::class.java)
                trySend(listeUsers)
            }
        }
        // Coupe la connexion à Firestore
        awaitClose { subscription.remove() }
    }

    // écoute un joueur en temps réel (pour afficher son solde)
    fun ecouterUser(uid: String): Flow<User?> = callbackFlow {
        val listener = usersRef.document(uid).addSnapshotListener { snapshot, _ ->
            val user = snapshot?.toObject(User::class.java)
            trySend(user)
        }
        awaitClose { listener.remove() }
    }

    // ajoute de l'argent virtuel au solde du joueur (directement sur le serveur)
    fun ajouterAuSolde(uid: String, montant: Int) {
        usersRef.document(uid).update("solde", FieldValue.increment(montant.toLong()))
    }
}