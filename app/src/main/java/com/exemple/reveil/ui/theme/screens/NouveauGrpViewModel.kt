package com.exemple.reveil.ui.theme.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class Ami(
    val id: Int,
    val nom: String,
    val selectionne: Boolean = false
)

class NouveauGrpViewModel : ViewModel() {
    var nomGroupe by mutableStateOf("")
        private set

    var champOvale by mutableStateOf("")
        private set

    var alarmeActivee by mutableStateOf(false)
        private set

    var heure by mutableIntStateOf(7)
        private set

    var minute by mutableIntStateOf(30)
        private set

    val heureAffichee: String
        get() = String.format("%02d:%02d", heure, minute)

    val listeAmis = mutableStateListOf(
        Ami(id = 1, nom = "Aliona", selectionne = false),
        Ami(id = 2, nom = "Ines", selectionne = false)
    )

    fun onNomGroupeChange(nouveauNom: String) {
        nomGroupe = nouveauNom
    }

    fun onChampOvaleChange(nouveauTexte: String) {
        champOvale = nouveauTexte
    }

    fun onAlarmeToggle(active: Boolean) {
        alarmeActivee = active
    }

    fun updateHeure(nouvelleHeure: Int, nouvelleMinute: Int) {
        heure = nouvelleHeure
        minute = nouvelleMinute
    }

    fun toggleAmiSelection(index: Int, isChecked: Boolean) {
        val ami = listeAmis[index]
        listeAmis[index] = ami.copy(selectionne = isChecked)
        Log.d("CHECKBOXES", "${ami.nom} sélectionné: $isChecked")
    }

    fun creerGroupe() {
        val membresChoisis = listeAmis.filter { it.selectionne }.map { it.nom }
        Log.d("GROUPE", "Création groupe '$nomGroupe' avec : $membresChoisis à $heureAffichee")
    }
}