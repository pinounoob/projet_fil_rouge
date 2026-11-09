package com.exemple.reveil.ui.screens.alarme

import com.exemple.reveil.R
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.reveil.data.GroupeRepository
import com.exemple.reveil.data.Resultat
import com.exemple.reveil.data.ResultatRepository
import com.exemple.reveil.data.UserRepository
import com.exemple.reveil.logique.calculerGains
import com.exemple.reveil.data.Matin
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

//construit le classement à partir des réveils du jour
//renvoie les lignes du podium et la cagnotte restante
fun construirePodium(resultats: List<Resultat>, cagnotte: Int): Pair<List<LignePodium>, Int> {
    val tries = resultats.sortedBy { it.heureArret }          // le plus tôt en premier
    val calcul = calculerGains(tries.map { it.uid }, cagnotte)
    val lignes = calcul.gains.mapIndexed { index, gain ->
        LignePodium(
            rang = gain.rang,
            nom = tries[index].pseudo,
            gain = gain.montant
        )
    }
    return Pair(lignes, calcul.cagnotteRestante)
}

class AlarmeViewModel(application: Application) : AndroidViewModel(application) {

    // Récupère un texte de res/values/strings.xml
    private fun texte(id: Int, vararg args: Any): String =
        getApplication<Application>().getString(id, *args)

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

    private var matin: Matin? = null

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
                erreur = texte(R.string.erreur_charger_groupe)
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
                erreur = texte(R.string.erreur_charger_classement)
            }
        }

        //écouter si les gains du matin sont déjà distribués
        viewModelScope.launch {
            try {
                groupeRepository.ecouterMatin(groupeId, jour).collect {
                    matin = it
                    mettreAJourPodium()
                }
            } catch (e: Exception) {
                erreur = texte(R.string.erreur_charger_gains)
            }
        }
    }

    fun jeSuisDebout() {
        val user = auth.currentUser ?: return
        estDebout = true


        viewModelScope.launch {
            try {
                val pseudo = userRepository.getUser(user.uid)?.pseudo ?: texte(R.string.moi)
                resultatRepository.envoyerResultat(groupeId, user.uid, pseudo, jour)
                groupeRepository.distribuerGains(groupeId, jour)
            } catch (e: Exception) {
                erreur = texte(R.string.erreur_envoi_reveil)
            }
        }
    }

    private fun mettreAJourPodium() {
        val m = matin
        // après la distribution, on calcule avec la cagnotte d'avant
        val cagnottePodium = if (m != null && m.distribue) m.cagnotteAvant else cagnotte
        val (lignes, reste) = construirePodium(
            resultatsDuJour,
            cagnottePodium
        )
        podium = lignes
        cagnotteApresGains = reste
    }
}