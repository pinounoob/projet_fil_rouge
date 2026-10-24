package com.exemple.reveil.ui.theme.screens.alarme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.reveil.data.GroupeRepository
import com.exemple.reveil.data.Resultat
import com.exemple.reveil.data.ResultatRepository
import com.exemple.reveil.data.UserRepository
import com.exemple.reveil.calculerGains
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Une ligne du classement du matin
data class LignePodium(
    val rang: Int,
    val nom: String,
    val gain: Int
)

class AlarmeViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val groupeRepository = GroupeRepository()
    private val resultatRepository = ResultatRepository()
    private val userRepository = UserRepository()

    //états
    var nomGroupe by mutableStateOf("")
        private set

    var heureAlarme by mutableIntStateOf(7 * 60) // en minutes depuis minuit
        private set

    var cagnotte by mutableIntStateOf(0)
        private set

    var cagnotteApresGains by mutableIntStateOf(0)
        private set

    var estDebout by mutableStateOf(false)
        private set

    var podium by mutableStateOf(listOf<LignePodium>())
        private set

    var erreur by mutableStateOf<String?>(null)
        private set

    private var groupeId = ""
    private var resultatsDuJour = listOf<Resultat>()
    private val jour = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE).format(Date())

    //logique

    fun chargerGroupe(groupeId: String) {
        this.groupeId = groupeId

        //ecouter le groupe (nom, heure, cagnotte)
        viewModelScope.launch {
            try {
                groupeRepository.ecouterGroupe(groupeId).collect { groupe ->
                    if (groupe != null) {
                        nomGroupe = groupe.nom
                        heureAlarme = groupe.heureAlarme
                        cagnotte = groupe.cagnotte
                        mettreAJourPodium()
                    }
                }
            } catch (e: Exception) {
                erreur = "Impossible de charger le groupe."
            }
        }

        //ecouter les réveils du jour des membres
        viewModelScope.launch {
            try {
                resultatRepository.ecouterResultatsDuJour(groupeId, jour).collect { resultats ->
                    resultatsDuJour = resultats
                    mettreAJourPodium()
                }
            } catch (e: Exception) {
                erreur = "Impossible de charger le classement."
            }
        }
    }

    fun jeSuisDebout() {
        val user = auth.currentUser ?: return
        estDebout = true


        viewModelScope.launch {
            try {
                val pseudo = userRepository.getUser(user.uid)?.pseudo ?: "Moi"
                resultatRepository.envoyerResultat(groupeId, user.uid, pseudo, jour)
            } catch (e: Exception) {
                erreur = "Ton réveil n'a pas pu être envoyé. Vérifie ta connexion."
            }
        }
    }

    private fun mettreAJourPodium() {
        //le plus tôt en premier
        val tries = resultatsDuJour.sortedBy { it.heureArret }
        val calcul = calculerGains(tries.map { it.uid }, cagnotte)

        podium = calcul.gains.mapIndexed { index, gain ->
            LignePodium(rang = gain.rang, nom = tries[index].pseudo, gain = gain.montant)
        }
        cagnotteApresGains = calcul.cagnotteRestante
    }
}