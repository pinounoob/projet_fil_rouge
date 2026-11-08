package com.exemple.reveil.ui.screens.login

import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import android.util.Log
import com.exemple.reveil.R
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.reveil.data.PreferenceManager
import com.exemple.reveil.data.User
import com.exemple.reveil.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.text.ifEmpty

data class LoginUiState(
    val email: String = "",
    val pseudo: String = "",
    val motDePasse: String = "",
    val isPasswordVisible: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoading: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // Récupère un texte de res/values/strings.xml
    private fun texte(id: Int, vararg args: Any): String =
        getApplication<Application>().getString(id, *args)
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()
    private val preferenceManager = PreferenceManager(application)

    fun onEmailChange(nouveau: String) {
        _uiState.update { it.copy(email = nouveau, errorMessage = null) }
    }

    fun onPseudoChange(nouveau: String) {
        _uiState.update { it.copy(pseudo = nouveau, errorMessage = null) }
    }

    fun onMotDePasseChange(nouveau: String) {
        _uiState.update { it.copy(motDePasse = nouveau, errorMessage = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun sinscrire() {
        val email = _uiState.value.email.trim()
        val mdp = _uiState.value.motDePasse
        val pseudo = _uiState.value.pseudo.trim()

        if (email.isEmpty() || mdp.isEmpty() || pseudo.isEmpty()) {
            _uiState.update { it.copy(errorMessage = texte(R.string.erreur_champs_inscription)) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            try {
                // 1. Créer le compte (on est alors connecté, donc on a le droit de lire users)
                val user = auth.createUserWithEmailAndPassword(email, mdp).await().user
                    ?: throw Exception(texte(R.string.erreur_inscription))

                // 2. Vérifier le pseudo (en ignorant notre propre uid)
                if (userRepository.pseudoPris(pseudo, excepteUid = user.uid)) {
                    user.delete().await()          // on annule le compte qu'on vient de créer
                    _uiState.update { it.copy(isLoading = false, errorMessage = texte(R.string.ce_pseudo_est_deja_utilise)) }
                    return@launch
                }

                // 3. Enregistrer le profil et envoyer l'e-mail de vérification
                userRepository.addUser(User(uid = user.uid, pseudo = pseudo, email = email))
                user.sendEmailVerification()
                auth.signOut()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = texte(R.string.compte_cree),
                        errorMessage = null
                    )
                }
            } catch (e: FirebaseAuthUserCollisionException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = texte(R.string.email_deja_utilise)) }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Inscription échouée", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: texte(R.string.erreur_inscription)) }
            }
        }
    }

    fun seConnecter(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        val mdp = _uiState.value.motDePasse

        if (email.isEmpty() || mdp.isEmpty()) {
            _uiState.update { it.copy(errorMessage = texte(R.string.erreur_champs_connexion)) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        auth.signInWithEmailAndPassword(email, mdp)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        if (user.isEmailVerified) {
                            viewModelScope.launch {
                                try {
                                    val dbUser = userRepository.getUser(user.uid)
                                    val vraiPseudo = dbUser?.pseudo?.ifEmpty { null }
                                        ?: user.email?.substringBefore("@")
                                        ?: texte(R.string.utilisateur)

                                    preferenceManager.saveSession(uid = user.uid, pseudo = vraiPseudo, stayConnected = true)
                                    _uiState.update { it.copy(isLoading = false) }
                                    onSuccess()
                                } catch (e: Exception) {
                                    val fallbackPseudo = user.email?.substringBefore("@") ?: texte(R.string.utilisateur)
                                    preferenceManager.saveSession(uid = user.uid, pseudo = fallbackPseudo, stayConnected = true)
                                    _uiState.update { it.copy(isLoading = false) }
                                    onSuccess()
                                }
                            }
                        } else {
                            auth.signOut()
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = texte(R.string.email_non_verifie),
                                    successMessage = null
                                )
                            }
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = texte(R.string.identifiants_incorrects)) }
                }
            }
    }

    fun reinitialiserMotDePasse() {
        val email = _uiState.value.email.trim()

        if (email.isEmpty()) {
            _uiState.update { it.copy(errorMessage = texte(R.string.email_reinitialisation_manquant)) }
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.update { it.copy(successMessage = texte(R.string.email_reinitialisation_envoye)) }
                } else {
                    _uiState.update { it.copy(errorMessage = task.exception?.localizedMessage ?: texte(R.string.erreur_envoi_email)) }
                }
            }
    }
}