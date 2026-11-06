package com.exemple.reveil.logique

// gains prévus pour le 1er, le 2e et le 3e
val GAINS_PAR_RANG = listOf(5, 3, 2)

// le gain d'un joueur pour un matin
data class Gain(
    val joueur: String,
    val rang: Int,
    val montant: Int
)

// les gains de chacun et ce qui reste dans la cagnotte
data class ResultatCalcul(
    val gains: List<Gain>,
    val cagnotteRestante: Int
)

//Calcule les gains du matin et retourne les gains de chaque joueur et la cagnotte restante
fun calculerGains(classement: List<String>, cagnotte: Int): ResultatCalcul {
    require(cagnotte >= 0) { "La cagnotte ne peut pas être négative" }

    var reste = cagnotte
    val gains = classement.mapIndexed { index, joueur ->
        val prevu = GAINS_PAR_RANG.getOrElse(index) { 0 }   // 5, 3, 2, puis 0
        val montant = minOf(prevu, reste)                    // éviter que la cagnotte soit négative
        reste -= montant
        Gain(joueur = joueur, rang = index + 1, montant = montant)
    }
    return ResultatCalcul(gains = gains, cagnotteRestante = reste)
}