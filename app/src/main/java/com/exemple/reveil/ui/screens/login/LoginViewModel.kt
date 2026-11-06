package com.exemple.reveil.ui.screens.login

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
            _uiState.update { it.copy(errorMessage = "Veuillez remplir Email, Identifiant et Mot de passe.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            try {
                if (userRepository.pseudoPris(pseudo)) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Ce pseudo est déjà utilisé.") }
                    return@launch
                }

                auth.createUserWithEmailAndPassword(email, mdp)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = task.result?.user
                            if (user != null) {
                                user.sendEmailVerification()

                                viewModelScope.launch {
                                    try {
                                        userRepository.addUser(
                                            User(
                                                uid = user.uid,
                                                pseudo = pseudo,
                                                email = email
                                            )
                                        )

                                        auth.signOut()

                                        _uiState.update {
                                            it.copy(
                                                isLoading = false,
                                                successMessage = "Compte créé ! Vérifiez votre boîte mail pour valider votre compte.",
                                                errorMessage = null
                                            )
                                        }
                                    } catch (e: Exception) {
                                        _uiState.update {
                                            it.copy(
                                                isLoading = false,
                                                errorMessage = "Erreur d'écriture dans la base de données"
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            _uiState.update { it.copy(isLoading = false, errorMessage = task.exception?.localizedMessage ?: "Erreur d'inscription") }
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Pseudo déjà utilisé") }
            }
        }
    }

    fun seConnecter(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        val mdp = _uiState.value.motDePasse

        if (email.isEmpty() || mdp.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Veuillez renseigner votre email et mot de passe.") }
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
                                        ?: "Utilisateur"

                                    preferenceManager.saveSession(uid = user.uid, pseudo = vraiPseudo, stayConnected = true)
                                    _uiState.update { it.copy(isLoading = false) }
                                    onSuccess()
                                } catch (e: Exception) {
                                    val fallbackPseudo = user.email?.substringBefore("@") ?: "Utilisateur"
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
                                    errorMessage = "Vous devez valider votre adresse email en cliquant sur le lien reçu avant de pouvoir vous connecter.",
                                    successMessage = null
                                )
                            }
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Identifiants incorrects ou compte inexistant.") }
                }
            }
    }

    fun reinitialiserMotDePasse() {
        val email = _uiState.value.email.trim()

        if (email.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Veuillez renseigner votre adresse email pour réinitialiser le mot de passe.") }
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.update { it.copy(successMessage = "Un email de réinitialisation a été envoyé.") }
                } else {
                    _uiState.update { it.copy(errorMessage = task.exception?.localizedMessage ?: "Erreur lors de l'envoi de l'email.") }
                }
            }
    }
}