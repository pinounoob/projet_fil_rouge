package com.exemple.reveil.ui.theme.screens.login
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
data class LoginUiState(
    val identifiant: String = "",
    val motDePasse: String = "",
    val isPasswordVisible: Boolean = false
)

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onIdentifiantChange(nouveau: String) {
        _uiState.update { it.copy(identifiant = nouveau) }
    }

    fun onMotDePasseChange(nouveau: String) {
        _uiState.update { it.copy(motDePasse = nouveau) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login() {
        val currentId = _uiState.value.identifiant
        val currentMdp = _uiState.value.motDePasse
        println("Tentative de connexion avec $currentId")
    }
}
