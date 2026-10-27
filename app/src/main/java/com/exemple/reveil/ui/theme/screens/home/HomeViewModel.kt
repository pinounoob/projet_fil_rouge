package com.exemple.reveil.ui.theme.screens.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.reveil.data.Groupe
import com.exemple.reveil.data.GroupeRepository
import com.exemple.reveil.data.PreferenceManager
import com.exemple.reveil.data.Resultat
import com.exemple.reveil.data.ResultatRepository
import com.exemple.reveil.data.User
import com.exemple.reveil.data.UserRepository
import com.exemple.reveil.ui.theme.screens.alarme.LignePodium
import com.exemple.reveil.ui.theme.screens.alarme.construirePodium
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val groupeRepository = GroupeRepository()
    private val resultatRepository = ResultatRepository()
    private val userRepository = UserRepository()
    private val preferenceManager = PreferenceManager(application)

    //états
    var recherche by mutableStateOf("")
        private set

    var groupes by mutableStateOf(listOf<Groupe>())
        private set

    var groupeSelectionne by mutableStateOf<Groupe?>(null)
        private set

    var menuGroupeOuvert by mutableStateOf(false)
        private set

    var contacts by mutableStateOf(listOf<String>())
        private set

    var classementDuJour by mutableStateOf(listOf<LignePodium>())
        private set

    var chargement by mutableStateOf(true)
        private set

    var erreur by mutableStateOf<String?>(null)
        private set

    var solde by mutableIntStateOf(0)
        private set

    private var tousLesJoueurs = listOf<User>()
    private var resultatsDuJour = listOf<Resultat>()
    private var ecouteResultats: Job? = null

    private var miseEnCours = false

    private val jour = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE).format(Date())

    init {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            chargement = false
        } else {
            //les groupes du joueur
            viewModelScope.launch {
                val idEnregistre = preferenceManager.selectedGroupFlow.first()
                try {
                    groupeRepository.ecouterGroupesDuJoueur(uid).collect { liste ->
                        groupes = liste
                        chargement = false
                        //on garde le groupe déjà affiché, sinon celui enregistré
                        val idVoulu = groupeSelectionne?.id ?: idEnregistre
                        val choisi = liste.find { it.id == idVoulu } ?: liste.firstOrNull()
                        selectionner(choisi)
                    }
                } catch (e: Exception) {
                    erreur = "Impossible de charger tes groupes."
                    chargement = false
                }
            }

            //les joueurs
            viewModelScope.launch {
                try {
                    userRepository.getAllUsers().collect { joueurs ->
                        tousLesJoueurs = joueurs
                        mettreAJourContacts()
                    }
                } catch (e: Exception) {
                    erreur = "Impossible de charger les membres."
                }
            }

            //le solde du joueur
            viewModelScope.launch {
                try {
                    userRepository.ecouterUser(uid).collect { user ->
                        solde = user?.solde ?: 0
                    }
                } catch (e: Exception) {
                    erreur = "Impossible de charger ton solde."
                }
            }
        }
    }

    //logique

    fun changerRecherche(texte: String) {
        recherche = texte
    }

    fun changerMenuGroupe(ouvert: Boolean) {
        menuGroupeOuvert = ouvert
    }

    fun choisirGroupe(groupe: Groupe) {
        menuGroupeOuvert = false
        selectionner(groupe)
        viewModelScope.launch { preferenceManager.saveSelectedGroup(groupe.id) }
    }

    fun ajouterCagnotte() {
        val uid = auth.currentUser?.uid ?: return
        val groupe = groupeSelectionne ?: return
        if (miseEnCours) return   // évite les doubles appuis

        //vérification rapide avant d'appeler Firestore
        if (solde < groupe.mise) {
            erreur = "Solde insuffisant : ajoute de l'argent dans les paramètres."
            return
        }

        miseEnCours = true
        viewModelScope.launch {
            try {
                groupeRepository.miser(groupe.id, uid)
                erreur = null
            } catch (e: FirebaseFirestoreException) {
                erreur = if (e.code == FirebaseFirestoreException.Code.ABORTED) {
                    "Solde insuffisant : ajoute de l'argent dans les paramètres."
                } else {
                    "La mise n'a pas pu être enregistrée."
                }
            } catch (e: Exception) {
                erreur = "La mise n'a pas pu être enregistrée. Vérifie ta connexion."
            } finally {
                miseEnCours = false
            }
        }
    }

    fun ajouterAlarme() {

    }

    fun ajouterContact() {

    }

    private fun selectionner(groupe: Groupe?) {
        val ancienId = groupeSelectionne?.id
        groupeSelectionne = groupe
        mettreAJourContacts()

        if (groupe == null) {
            classementDuJour = emptyList()
            return
        }

        //nouveau groupe choisi : on écoute ses résultats à lui
        if (groupe.id != ancienId) {
            resultatsDuJour = emptyList()
            ecouteResultats?.cancel()
            ecouteResultats = viewModelScope.launch {
                try {
                    resultatRepository.ecouterResultatsDuJour(groupe.id, jour).collect { resultats ->
                        resultatsDuJour = resultats
                        mettreAJourClassement()
                    }
                } catch (e: Exception) {
                    erreur = "Impossible de charger le classement."
                }
            }
        }
        mettreAJourClassement()
    }

    private fun mettreAJourClassement() {
        val cagnotte = groupeSelectionne?.cagnotte ?: 0
        val (lignes, _) = construirePodium(resultatsDuJour, cagnotte)
        classementDuJour = lignes
    }

    private fun mettreAJourContacts() {
        val membres = groupeSelectionne?.membres ?: emptyList()
        val monUid = auth.currentUser?.uid
        contacts = tousLesJoueurs
            .filter { it.uid in membres && it.uid != monUid }
            .map { it.pseudo }
    }
}