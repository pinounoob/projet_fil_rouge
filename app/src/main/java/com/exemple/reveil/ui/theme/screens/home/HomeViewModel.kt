package com.exemple.reveil.ui.theme.screens.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.reveil.data.GroupeData
import com.exemple.reveil.data.GroupeRepository
import com.exemple.reveil.data.User
import com.exemple.reveil.data.UserRepository
import com.exemple.reveil.ui.theme.screens.alarme.LignePodium
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val groupeRepository = GroupeRepository()
    private val auth = FirebaseAuth.getInstance()

    var recherche by mutableStateOf("")
        private set

    var groupeSelectionne by mutableStateOf<GroupeData?>(null)
        private set

    var chargement by mutableStateOf(false)
        private set

    var nom by mutableStateOf("Utilisateur")
        private set

    var solde by mutableIntStateOf(0)
        private set

    var cagnotte by mutableIntStateOf(0)
        private set

    var mise by mutableIntStateOf(5)
        private set

    var heureAlarme by mutableStateOf("07:30")
        private set

    var erreur by mutableStateOf<String?>(null)
        private set

    var menuGroupeOuvert by mutableStateOf(false)
        private set

    var popupContactOuvert by mutableStateOf(false)
        private set

    var rechercheContact by mutableStateOf("")
        private set

    var selecteurHeureOuvert by mutableStateOf(false)
        private set

    val groupes = mutableStateListOf<GroupeData>()
    val contacts = mutableStateListOf<User>()
    val classementsDuJour = mutableStateListOf<LignePodium>()
    val joueursProposes = mutableStateListOf<User>()

    init {
        chargerProfilEtGroupes()
    }

    private fun chargerProfilEtGroupes() {
        val currentUid = auth.currentUser?.uid ?: return
        chargement = true

        viewModelScope.launch {
            try {
                userRepository.ecouterUser(currentUid).collect { user ->
                    if (user != null) {
                        nom = user.pseudo
                        solde = user.solde
                    }
                }
            } catch (e: Exception) {
                erreur = e.localizedMessage
            }
        }

        viewModelScope.launch {
            try {
                groupeRepository.ecouterGroupesDuJoueur(currentUid).collect { liste ->
                    groupes.clear()
                    groupes.addAll(liste)
                    if (liste.isNotEmpty() && groupeSelectionne == null) {
                        choisirGroupe(liste.first())
                    }
                    chargement = false
                }
            } catch (e: Exception) {
                chargement = false
                erreur = e.localizedMessage
            }
        }

        viewModelScope.launch {
            try {
                val tousLesUsers = userRepository.getAllUsers().first()
                joueursProposes.clear()
                joueursProposes.addAll(tousLesUsers.filter { it.uid != currentUid })
            } catch (e: Exception) {
                // Ignorer ou logguer l'erreur
            }
        }
    }

    fun changerRecherche(nouvelleRecherche: String) {
        recherche = nouvelleRecherche
    }

    fun changerMenuGroupe(ouvert: Boolean) {
        menuGroupeOuvert = ouvert
    }

    fun choisirGroupe(groupe: GroupeData) {
        groupeSelectionne = groupe
        mise = groupe.mise
        cagnotte = groupe.mise * groupe.membres.size
        heureAlarme = "%02d:%02d".format(groupe.heure, groupe.minute)
        menuGroupeOuvert = false
    }

    fun ajouterCagnotte() {}

    fun ajouterAlarme() {}

    fun ajouterContact() {
        popupContactOuvert = true
    }

    fun changerRechercheContact(texte: String) {
        rechercheContact = texte
    }

    fun choisirContact(user: User) {
        if (!contacts.contains(user)) {
            contacts.add(user)
        }
        popupContactOuvert = false
    }

    fun fermerPopupContact() {
        popupContactOuvert = false
    }

    fun modifierHeure(heure: Int, minute: Int) {
        selecteurHeureOuvert = false
        val gId = groupeSelectionne?.id ?: return
        viewModelScope.launch {
            groupeRepository.modifierHeure(gId, heure, minute)
        }
    }

    fun fermerSelecteurHeure() {
        selecteurHeureOuvert = false
    }
}