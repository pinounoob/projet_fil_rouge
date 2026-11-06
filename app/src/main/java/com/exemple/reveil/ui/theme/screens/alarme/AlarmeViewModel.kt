package com.exemple.reveil.ui.theme.screens.alarme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.reveil.data.GroupeData
import com.exemple.reveil.data.GroupeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AlarmeUiState(
    val groupe: GroupeData? = null,
    val heureFormatee: String = "07:30",
    val cagnotteTotale: Int = 0,
    val estReveille: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class AlarmeViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AlarmeUiState())
    val uiState: StateFlow<AlarmeUiState> = _uiState.asStateFlow()

    private val groupeRepository = GroupeRepository()

    fun chargerGroupe(groupeId: String) {
        if (groupeId.isEmpty()) return

        viewModelScope.launch {
            try {
                groupeRepository.ecouterGroupe(groupeId).collect { groupe ->
                    if (groupe != null) {
                        val heureStr = "%02d:%02d".format(groupe.heure, groupe.minute)
                        val totalCagnotte = groupe.mise * groupe.membres.size

                        _uiState.update {
                            it.copy(
                                groupe = groupe,
                                heureFormatee = heureStr,
                                cagnotteTotale = totalCagnotte,
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Groupe introuvable."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erreur de chargement : ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun validerReveil() {
        _uiState.update { it.copy(estReveille = true) }
    }
}