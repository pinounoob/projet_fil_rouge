package com.exemple.reveil.data

//un matin d'un groupe
data class Matin(
    val distribue: Boolean = false,
    val cagnotteAvant: Int = 0,   // cagnotte au moment de la distribution (pour afficher le podium)
    val participants: List<String> = emptyList()   // les uid de ceux qui avaient misé ce matin-là
)