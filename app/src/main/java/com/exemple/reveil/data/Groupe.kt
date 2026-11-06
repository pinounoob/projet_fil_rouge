package com.exemple.reveil.data

import com.google.firebase.firestore.DocumentId

data class Groupe(
    @DocumentId val id: String = "",
    val nom: String = "",
    val heureAlarme: Int = 7 * 60,          //en minutes depuis minuit
    val alarmeActive: Boolean = true,
    val mise: Int = 10,                     //en euros
    val cagnotte: Int = 0,                  //en euros
    val membres: List<String> = emptyList() //les uid des joueurs
)