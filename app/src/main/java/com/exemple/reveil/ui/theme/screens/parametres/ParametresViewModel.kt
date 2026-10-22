package com.exemple.reveil.ui.theme.screens.parametres

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.reveil.data.PreferenceManager
import com.exemple.reveil.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ParametresUiState(
    val themeChoice: String = "SYSTEME",
    val pseudo: String = "",
    val defaultBet: Int = 10,
    val ringtoneMode: String = "SONNERIE"
)

class ParametresViewModel(application: Application) : AndroidViewModel(application) {

    private val preferenceManager = PreferenceManager(application)
    private val userRepository = UserRepository() // Ajout du Repository Firestore
    private val auth = FirebaseAuth.getInstance() // Ajout de l'Auth pour connaître l'utilisateur actuel

    // Le stateIn reste identique, il lit toujours depuis le DataStore local
    val uiState: StateFlow<ParametresUiState> = combine(
        preferenceManager.themeFlow,
        preferenceManager.pseudoFlow,
        preferenceManager.defaultBetFlow,
        preferenceManager.ringtoneModeFlow
    ) { theme, pseudo, bet, ringtone ->
        ParametresUiState(theme, pseudo, bet, ringtone)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ParametresUiState()
    )

    fun updateTheme(newTheme: String) {
        viewModelScope.launch { preferenceManager.saveTheme(newTheme) }
    }

    fun updateDefaultBet(newBet: Int) {
        viewModelScope.launch { preferenceManager.saveDefaultBet(newBet) }
    }

    fun updateRingtoneMode(newMode: String) {
        viewModelScope.launch { preferenceManager.saveRingtoneMode(newMode) }
    }

    fun clearSession() {
        viewModelScope.launch { preferenceManager.clearSession() }
    }

    fun updatePseudo(newPseudo: String) {
        viewModelScope.launch {
            preferenceManager.savePseudo(newPseudo)
            val currentUser = auth.currentUser
            if (currentUser != null) {
                try {
                    userRepository.updateUser(currentUser.uid, newPseudo)
                } catch (e: Exception) {
                    // Optionnel : gérer l'erreur (ex: si pas d'internet)
                    Log.e("Parametres", "Erreur lors de la mise à jour Firestore", e)
                }
            }
        }
    }
}