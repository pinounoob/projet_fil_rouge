package com.exemple.reveil.ui.theme.screens.home

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.reveil.data.GroupeData
import com.exemple.reveil.data.GroupeRepository
import com.exemple.reveil.util.AlarmeScheduler
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val groupeRepository = GroupeRepository()
    private val auth = FirebaseAuth.getInstance()

    var groupes by mutableStateOf<List<GroupeData>>(emptyList())
        private set

    var groupeSelectionne by mutableStateOf<GroupeData?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        chargerGroupes()
    }

    private fun chargerGroupes() {
        val currentUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            groupeRepository.ecouterGroupesDuJoueur(currentUid).collectLatest { liste ->
                groupes = liste
                if (groupeSelectionne == null && liste.isNotEmpty()) {
                    groupeSelectionne = liste.first()
                } else if (groupeSelectionne != null) {
                    groupeSelectionne = liste.find { it.id == groupeSelectionne?.id } ?: liste.firstOrNull()
                }
            }
        }
    }

    fun melectionnerGroupe(groupe: GroupeData) {
        groupeSelectionne = groupe
    }

    // Modifier l'heure du groupe sélectionné
    fun modifierHeureGroupe(nouvelleHeure: Int, nouvelleMinute: Int) {
        val groupeId = groupeSelectionne?.id ?: return

        viewModelScope.launch {
            try {
                // 1. Mise à jour dans Firestore
                groupeRepository.modifierHeure(groupeId, nouvelleHeure, nouvelleMinute)

                // 2. Reprogrammation de l'alarme système
                AlarmeScheduler.programmerAlarme(
                    getApplication(),
                    groupeId,
                    nouvelleHeure,
                    nouvelleMinute
                )
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Erreur lors de la modification de l'heure", e)
            }
        }
    }
}