package com.exemple.reveil.ui.theme.screens.nouveaugroupe

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.reveil.data.GroupeRepository
import com.exemple.reveil.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AmiUiModel(
    val uid: String,
    val pseudo: String,
    val selectionne: Boolean = false
)

class NouveauGrpViewModel(application: Application) : AndroidViewModel(application) {

    private val groupeRepository = GroupeRepository()
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    var nomGroupe by mutableStateOf("")
        private set

    var mise by mutableIntStateOf(5)
        private set

    var heure by mutableIntStateOf(7)
        private set

    var minute by mutableIntStateOf(30)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val listeAmis = mutableStateListOf<AmiUiModel>()

    init {
        chargerUtilisateurs()
    }

    private fun chargerUtilisateurs() {
        viewModelScope.launch {
            try {
                val currentUid = auth.currentUser?.uid ?: ""
                val tousLesUsers = userRepository.getAllUsers().first()

                val amis = tousLesUsers
                    .filter { it.uid != currentUid }
                    .map { AmiUiModel(uid = it.uid, pseudo = it.pseudo) }

                listeAmis.clear()
                listeAmis.addAll(amis)
            } catch (e: Exception) {
                Log.e("NouveauGrpVM", "Erreur chargement utilisateurs", e)
            }
        }
    }

    fun onNomGroupeChange(nouveauNom: String) {
        nomGroupe = nouveauNom
        errorMessage = null
    }

    fun updateHeure(nouvelleHeure: Int, nouvelleMinute: Int) {
        heure = nouvelleHeure
        minute = nouvelleMinute
    }

    fun toggleAmiSelection(index: Int, isChecked: Boolean) {
        if (index in listeAmis.indices) {
            val ami = listeAmis[index]
            listeAmis[index] = ami.copy(selectionne = isChecked)
        }
    }

    fun creerGroupe(onSuccess: () -> Unit) {
        val nom = nomGroupe.trim()
        val currentUid = auth.currentUser?.uid ?: ""

        if (nom.isEmpty()) {
            errorMessage = "Veuillez entrer un nom de groupe."
            return
        }

        val membresSelectionnesUids = listeAmis.filter { it.selectionne }.map { it.uid }.toMutableList()
        if (currentUid.isNotEmpty()) {
            membresSelectionnesUids.add(currentUid)
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                groupeRepository.creerGroupe(
                    nom = nom,
                    mise = mise,
                    membresUid = membresSelectionnesUids
                )

                isLoading = false
                onSuccess()
            } catch (e: Exception) {
                Log.e("NouveauGrpVM", "Erreur lors de la création du groupe", e)
                isLoading = false
                errorMessage = "Erreur : ${e.localizedMessage ?: "Impossible de créer le groupe"}"
            }
        }
    }
}