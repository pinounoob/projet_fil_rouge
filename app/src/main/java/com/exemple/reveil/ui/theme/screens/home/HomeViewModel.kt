package com.exemple.reveil.ui.theme.screens.home

import com.exemple.reveil.ui.theme.screens.alarme.LignePodium
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel(){

    //états
    var recherche by mutableStateOf("")
        private set

    var groupes by mutableStateOf(listOf("Groupe Famille", "Groupe Amis", "Groupe Travail"))
        private set

    var groupeSelectionne by mutableStateOf("Groupe Famille")
        private set

    var menuGroupeOuvert by mutableStateOf(false)
        private set

    var cagnotte by mutableIntStateOf(150)
        private set

    var alarmeMinutes by mutableIntStateOf(7 * 60)
        private set

    var contacts by mutableStateOf(listOf("Marie", "Lucas"))
        private set

    var classementDuJour by mutableStateOf(
        listOf(
            LignePodium(rang = 1, nom = "Marie", gain = 5),
            LignePodium(rang = 2, nom = "Toi", gain = 3),
            LignePodium(rang = 3, nom = "Lucas", gain = 2)
        )
    )
        private set

    //logique

    fun changerRecherche(texte: String) {
        recherche = texte
    }

    fun ajouterGroupe() {
        groupes = groupes + "Groupe ${groupes.size + 1}"
    }

    fun changerMenuGroupe(ouvert: Boolean) {
        menuGroupeOuvert = ouvert
    }

    fun choisirGroupe(groupe: String) {
        groupeSelectionne = groupe
        menuGroupeOuvert = false
    }

    fun ajouterCagnotte() {
        cagnotte += 10
    }

    fun ajouterAlarme() {
        alarmeMinutes = (alarmeMinutes + 15) % (24 * 60)
    }

    fun ajouterContact() {
        contacts = contacts + "Contact ${contacts.size + 1}"
    }
}