package com.exemple.reveil.ui.theme.screens.alarme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.exemple.reveil.calculerGains

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
        nomGroupe = "Groupe $groupeId"
    }

    fun jeSuisDebout() {
        //ordre d'arrivée simulé
        val classement = listOf("Toi", "Aliona", "Kaouthar", "Lucas")

        val resultat = calculerGains(classement, cagnotte)

        podium = resultat.gains.map { gain ->
            LignePodium(rang = gain.rang, nom = gain.joueur, gain = gain.montant)
        }
        cagnotte = resultat.cagnotteRestante
        estDebout = true
    }
}