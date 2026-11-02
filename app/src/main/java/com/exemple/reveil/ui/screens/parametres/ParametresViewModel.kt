package com.exemple.reveil.ui.screens.parametres

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.reveil.data.PreferenceManager
import com.exemple.reveil.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ParametresUiState(
    val themeChoice: String = "SYSTEME",
    val pseudo: String = "",
    val defaultBet: Int = 10,
    val ringtoneMode: String = "SONNERIE",
    val solde: Int = 0,
    val ringtoneUri: String? = null,

    // variables pour les popups
    val showMoneyDialog: Boolean = false,
    val moneyInput: String = "",
    val showAccountDialog: Boolean = false,
    val newPseudoInput: String = "",
    val newEmailInput: String = "",
    val newPasswordInput: String = ""
)

private data class DialogState( // pour les popups
    val showMoneyDialog: Boolean = false,
    val moneyInput: String = "",
    val showAccountDialog: Boolean = false,
    val newPseudoInput: String = "",
    val newEmailInput: String = "",
    val newPasswordInput: String = ""
)

class ParametresViewModel(application: Application) : AndroidViewModel(application) {

    private val preferenceManager = PreferenceManager(application)
    private val userRepository = UserRepository() // Ajout du repository Firestore
    private val auth = FirebaseAuth.getInstance() // Ajout de l'Auth pour connaître l'utilisateur actuel
    private val _soldeFlow = MutableStateFlow(0)
    private val soldeFlow: Flow<Int> = _soldeFlow

    // écoute Firestore dès le lancement du ViewModel
    init {
        auth.currentUser?.uid?.let { uid ->
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && snapshot.exists()) {
                        val soldeFirestore = snapshot.getLong("solde")?.toInt() ?: 0
                        // maj flux
                        _soldeFlow.value = soldeFirestore
                    }
                }
        }
    }
    private val _dialogState = MutableStateFlow(DialogState())
    // Le stateIn reste identique, il lit toujours depuis le DataStore local
    val uiState: StateFlow<ParametresUiState> = combine(
        preferenceManager.themeFlow,
        preferenceManager.pseudoFlow,
        preferenceManager.defaultBetFlow,
        combine(
            preferenceManager.ringtoneModeFlow,
            soldeFlow,
            preferenceManager.ringtoneUriFlow
        ) { mode, solde, uri -> Triple(mode, solde, uri) },
        _dialogState
    ) { theme, pseudo, bet, triple, dialogs ->
        ParametresUiState(
            themeChoice = theme,
            pseudo = pseudo,
            defaultBet = bet,
            ringtoneMode = triple.first,
            solde = triple.second,
            ringtoneUri = triple.third,
            showMoneyDialog = dialogs.showMoneyDialog,
            moneyInput = dialogs.moneyInput,
            showAccountDialog = dialogs.showAccountDialog,
            newPseudoInput = dialogs.newPseudoInput,
            newEmailInput = dialogs.newEmailInput,
            newPasswordInput = dialogs.newPasswordInput
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ParametresUiState()
    )

    fun updateTheme(newTheme: String) {
        viewModelScope.launch { preferenceManager.saveTheme(newTheme) }
    }

    fun updateRingtoneMode(newMode: String) {
        viewModelScope.launch { preferenceManager.saveRingtoneMode(newMode) }
    }

    fun toggleRingtoneMode() {
        val currentMode = uiState.value.ringtoneMode
        val newMode = if (currentMode == "SONNERIE") "VIBRATION" else "SONNERIE"
        updateRingtoneMode(newMode) // Sauvegarde la nouvelle valeur dans le DataStore
    }

    fun updateRingtoneUri(uri: String) = viewModelScope.launch { preferenceManager.saveRingtoneUri(uri) }

    fun clearSession() {
        viewModelScope.launch { preferenceManager.clearSession() }
    }

    // Gestion popup ARGENT
    fun setShowMoneyDialog(show: Boolean) {
        _dialogState.update { it.copy(showMoneyDialog = show) }
    }

    fun updateMoneyInput(input: String) {
        _dialogState.update { it.copy(moneyInput = input) }
    }

    fun validerAjoutArgent() {
        val montant = _dialogState.value.moneyInput.toIntOrNull()
        if (montant != null && montant > 0) {
            viewModelScope.launch {
                val uid = auth.currentUser?.uid ?: return@launch
                try {
                    userRepository.ajouterAuSolde(uid, montant)
                } catch (e: Exception) {
                    Log.e("Parametres", "Ajout d'argent impossible", e)
                }
            }
        }
        // Ferme la popup et vide le champ
        _dialogState.update { it.copy(showMoneyDialog = false, moneyInput = "") }
    }

    // Gestion popup COMPTE
    fun setShowAccountDialog(show: Boolean) {
        _dialogState.update { it.copy(showAccountDialog = show) }
    }

    fun initAccountDialog() {
        _dialogState.update {
            it.copy(
                newPseudoInput = uiState.value.pseudo,
                newEmailInput = "",
                newPasswordInput = ""
            )
        }
    }

    fun updateNewPseudoInput(input: String) {
        _dialogState.update { it.copy(newPseudoInput = input) }
    }

    fun updateNewPasswordInput(input: String) {
        _dialogState.update { it.copy(newPasswordInput = input) }
    }

    fun validerModificationsCompte() {
        val currentPseudo = uiState.value.pseudo
        val newPseudo = _dialogState.value.newPseudoInput.trim()
        val newPassword = _dialogState.value.newPasswordInput

        // MaJ pseudo (Local + Firestore)
        if (newPseudo.isNotBlank() && newPseudo != currentPseudo) {
            viewModelScope.launch {
                preferenceManager.savePseudo(newPseudo)
                auth.currentUser?.let { user ->
                    try {
                        userRepository.updateUser(user.uid, newPseudo)
                    } catch (e: Exception) {
                        Log.e("Parametres", "Erreur lors de la mise à jour Firestore", e)
                    }
                }
            }
        }

        // MaJ mdp (Firebase Auth)
        if (newPassword.isNotBlank()) {
            auth.currentUser?.updatePassword(newPassword)?.addOnFailureListener {
                Log.e("Parametres", "Erreur MDP : Reconnexion récente requise.", it)
            }
        }

        // Ferme la popup et nettoie les champs
        _dialogState.update {
            it.copy(
                showAccountDialog = false,
                newPseudoInput = "",
                newEmailInput = "",
                newPasswordInput = ""
            )
        }
    }
}