package com.exemple.reveil.data

//un matin d'un groupe
data class Matin(
    val distribue: Boolean = false,
    val cagnotteAvant: Int = 0   // cagnotte au moment de la distribution (pour afficher le podium)
)