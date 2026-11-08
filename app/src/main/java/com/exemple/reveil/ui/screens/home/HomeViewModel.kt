package com.exemple.reveil.ui.screens.home

import com.exemple.reveil.R
import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.reveil.data.Groupe
import com.exemple.reveil.data.GroupeRepository
import com.exemple.reveil.data.Matin
import com.exemple.reveil.data.PreferenceManager
import com.exemple.reveil.data.Resultat
import com.exemple.reveil.data.ResultatRepository
import com.exemple.reveil.data.User
import com.exemple.reveil.data.UserRepository
import com.exemple.reveil.ui.screens.alarme.LignePodium
import com.exemple.reveil.ui.screens.alarme.construirePodium
import com.exemple.reveil.util.AlarmeScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Récupère un texte de res/values/strings.xml
    private fun texte(id: Int, vararg args: Any): String =
        getApplication<Application>().getString(id, *args)
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

    var contacts by mutableStateOf(listOf<User>())
        private set

    var contactARetirer by mutableStateOf<User?>(null)
        private set

    var classementDuJour by mutableStateOf(listOf<LignePodium>())
        private set

    var chargement by mutableStateOf(true)
        private set

    var erreur by mutableStateOf<String?>(null)
        private set

    var solde by mutableIntStateOf(0)
        private set

    private var tousLesJoueurs by mutableStateOf(listOf<User>())
    private var resultatsDuJour = listOf<Resultat>()
    private var ecouteResultats: Job? = null
    private val heuresProgrammees = mutableMapOf<String, Int>()   //groupeId -> heure déjà programmée

    private var miseEnCours = false

    private var ecouteMatin: Job? = null
    private var matin: Matin? = null

    private val jour = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE).format(Date())

    var popupContactOuvert by mutableStateOf(false)
        private set

    var rechercheContact by mutableStateOf("")
        private set

    var selecteurHeureOuvert by mutableStateOf(false)
        private set

    var confirmationQuitter by mutableStateOf(false)
        private set

    //les joueurs qu'on peut ajouter : correspondent à la recherche, ne sont pas déjà membres, et ce n'est pas moi
    val joueursProposes: List<User>
        get() {
            val texte = rechercheContact.trim()
            if (texte.isEmpty()) return emptyList()
            val membres = groupeSelectionne?.membres ?: emptyList()
            val monUid = auth.currentUser?.uid
            return tousLesJoueurs.filter {
                it.uid != monUid && it.uid !in membres && it.pseudo.isNotBlank() &&
                        it.pseudo.contains(texte, ignoreCase = true)
            }
        }

    init {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            chargement = false
        } else {
            // Synchronisation automatique du profil et du pseudo depuis Firestore
            viewModelScope.launch {
                try {
                    val dbUser = userRepository.getUser(uid)
                    if (dbUser != null && dbUser.pseudo.isNotBlank()) {
                        preferenceManager.savePseudo(dbUser.pseudo)
                    }
                } catch (e: Exception) {
                    Log.e("HomeVM", "Erreur sync pseudo", e)
                }
            }

            //les groupes du joueur
            viewModelScope.launch {
                val idEnregistre = preferenceManager.selectedGroupFlow.first()
                try {
                    groupeRepository.ecouterGroupesDuJoueur(uid).collect { liste ->
                        groupes = liste
                        chargement = false
                        programmerAlarmes(liste)
                        //on garde le groupe déjà affiché, sinon celui enregistré
                        val idVoulu = groupeSelectionne?.id ?: idEnregistre
                        val choisi = liste.find { it.id == idVoulu } ?: liste.firstOrNull()
                        selectionner(choisi)
                    }
                } catch (e: Exception) {
                    erreur = texte(R.string.erreur_charger_groupes)
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
                    erreur = texte(R.string.erreur_charger_membres)
                }
            }

            //le solde du joueur
            viewModelScope.launch {
                try {
                    userRepository.ecouterUser(uid).collect { user ->
                        solde = user?.solde ?: 0
                    }
                } catch (e: Exception) {
                    erreur = texte(R.string.erreur_charger_solde)
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
            erreur = texte(R.string.solde_insuffisant)
            return
        }

        miseEnCours = true
        viewModelScope.launch {
            try {
                groupeRepository.miser(groupe.id, uid)
                erreur = null
            } catch (e: FirebaseFirestoreException) {
                erreur = if (e.code == FirebaseFirestoreException.Code.ABORTED) {
                    texte(R.string.solde_insuffisant)
                } else {
                    texte(R.string.erreur_mise)
                }
            } catch (e: Exception) {
                erreur = texte(R.string.erreur_mise_connexion)
            } finally {
                miseEnCours = false
            }
        }
    }

    fun ajouterAlarme() {
        selecteurHeureOuvert = true
    }

    fun fermerSelecteurHeure() {
        selecteurHeureOuvert = false
    }

    //chaque téléphone programme SA sonnerie : on (re)programme quand un groupe apparaît ou que son heure change
    private fun programmerAlarmes(liste: List<Groupe>) {
        for (groupe in liste) {
            if (groupe.alarmeActive) {
                if (heuresProgrammees[groupe.id] != groupe.heureAlarme) {
                    heuresProgrammees[groupe.id] = groupe.heureAlarme
                    AlarmeScheduler.programmerAlarme(
                        getApplication<Application>(),
                        groupe.id,
                        groupe.heureAlarme / 60,
                        groupe.heureAlarme % 60
                    )
                }
            } else {
                if (heuresProgrammees.containsKey(groupe.id)) {
                    heuresProgrammees.remove(groupe.id)
                    AlarmeScheduler.annulerAlarme(getApplication<Application>(), groupe.id)
                }
            }
        }
    }

    fun changerAlarmeActive(active: Boolean) {
        val groupe = groupeSelectionne ?: return
        viewModelScope.launch {
            try {
                groupeRepository.modifierAlarmeActive(groupe.id, active)
                if (active) {
                    AlarmeScheduler.programmerAlarme(
                        getApplication<Application>(),
                        groupe.id,
                        groupe.heureAlarme / 60,
                        groupe.heureAlarme % 60
                    )
                    heuresProgrammees[groupe.id] = groupe.heureAlarme
                } else {
                    AlarmeScheduler.annulerAlarme(getApplication<Application>(), groupe.id)
                    heuresProgrammees.remove(groupe.id)
                }
                erreur = null
            } catch (e: Exception) {
                erreur = texte(R.string.erreur_modifier_alarme)
            }
        }
    }

    fun modifierHeure(minutes: Int) {
        val groupe = groupeSelectionne ?: return
        selecteurHeureOuvert = false
        viewModelScope.launch {
            try {
                groupeRepository.modifierHeure(groupe.id, minutes)
                erreur = null
            } catch (e: Exception) {
                erreur = texte(R.string.erreur_changer_heure)
            }
        }
    }

    fun ajouterContact() {
        rechercheContact = ""
        popupContactOuvert = true
    }

    fun fermerPopupContact() {
        popupContactOuvert = false
        rechercheContact = ""
    }

    fun changerRechercheContact(texte: String) {
        rechercheContact = texte
    }

    fun choisirContact(joueur: User) {
        val groupe = groupeSelectionne ?: return
        viewModelScope.launch {
            try {
                groupeRepository.ajouterMembre(groupe.id, joueur.uid)
                erreur = null
                fermerPopupContact()
            } catch (e: Exception) {
                erreur = texte(R.string.erreur_ajouter_joueur, joueur.pseudo)
            }
        }
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
                    erreur = texte(R.string.erreur_charger_classement)
                }
            }
            matin = null
            ecouteMatin?.cancel()
            ecouteMatin = viewModelScope.launch {
                try {
                    groupeRepository.ecouterMatin(groupe.id, jour).collect {
                        matin = it
                        mettreAJourClassement()
                    }
                } catch (e: Exception) {
                    erreur = texte(R.string.erreur_charger_gains)
                }
            }
        }
        mettreAJourClassement()
    }

    private fun mettreAJourClassement() {
        val m = matin
        val cagnotte = if (m != null && m.distribue) m.cagnotteAvant else (groupeSelectionne?.cagnotte ?: 0)
        val (lignes, _) = construirePodium(resultatsDuJour, cagnotte)
        classementDuJour = lignes
    }

    private fun mettreAJourContacts() {
        val membres = groupeSelectionne?.membres ?: emptyList()
        val monUid = auth.currentUser?.uid
        contacts = tousLesJoueurs
            .filter { it.uid in membres && it.uid != monUid }
    }

    //retirer un contact du groupe (avec confirmation)
    fun demanderRetrait(joueur: User) { contactARetirer = joueur }
    fun annulerRetrait() { contactARetirer = null }

    fun confirmerRetrait() {
        val joueur = contactARetirer ?: return
        val groupe = groupeSelectionne ?: return
        contactARetirer = null
        viewModelScope.launch {
            try {
                groupeRepository.retirerMembre(groupe.id, joueur.uid)
                erreur = null
            } catch (e: Exception) {
                erreur = texte(R.string.erreur_retirer_joueur, joueur.pseudo)
            }
        }
    }

    //quitter le groupe affiché (avec confirmation)
    fun demanderQuitter() { menuGroupeOuvert = false; confirmationQuitter = true }
    fun annulerQuitter() { confirmationQuitter = false }

    fun quitterGroupe() {
        val uid = auth.currentUser?.uid ?: return
        val groupe = groupeSelectionne ?: return
        confirmationQuitter = false
        viewModelScope.launch {
            try {
                groupeRepository.retirerMembre(groupe.id, uid)
                AlarmeScheduler.annulerAlarme(getApplication<Application>(), groupe.id)   // fonction de Kaouthar
                heuresProgrammees.remove(groupe.id)
                erreur = null
            } catch (e: Exception) {
                erreur = texte(R.string.erreur_quitter_groupe)
            }
        }
    }
}