package com.exemple.reveil.ui.theme.screens.alarme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// Une ligne du classement du matin
data class LignePodium(
    val rang: Int,
    val nom: String,
    val gain: Int
)

class AlarmeViewModel : ViewModel() {

    //états
    var nomGroupe by mutableStateOf("")
        private set

    var heureAlarme by mutableIntStateOf(7 * 60) // en minutes depuis minuit
        private set

    var cagnotte by mutableIntStateOf(150)
        private set

    var estDebout by mutableStateOf(false)
        private set

    var podium by mutableStateOf(listOf<LignePodium>())
        private set

    //logique

    fun chargerGroupe(groupeId: Int) {
        // Pour l'instant données factices
        nomGroupe = "Groupe $groupeId"
    }

    fun jeSuisDebout() {
        // Pour l'instant : classement simulé
        podium = listOf(
            LignePodium(rang = 1, nom = "Toi", gain = 5),
            LignePodium(rang = 2, nom = "Aliona", gain = 3),
            LignePodium(rang = 3, nom = "Kaouthar", gain = 2),
            LignePodium(rang = 4, nom = "Lucas", gain = 0)
        )
        cagnotte -= podium.sumOf { it.gain }
        estDebout = true
    }
}