package com.exemple.reveil.logique

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

// Calcule les gains du matin à partir du classement des joueurs qui ont misé
// (du premier levé au dernier). Toute la cagnotte est partagée en pourcentages :
//  - le dernier ne gagne rien (il perd sa mise)
//  - les joueurs du milieu récupèrent une part (1/n de la cagnotte = leur mise)
//  - le premier gagne 2 parts (2/n : sa mise + celle du dernier) + l'arrondi
// Exemples avec 10 € chacun : 2 joueurs → 20 / 0 ; 3 joueurs → 20 / 10 / 0
fun calculerGains(classement: List<String>, cagnotte: Int): ResultatCalcul {
    require(cagnotte >= 0) { "La cagnotte ne peut pas être négative" }

    val n = classement.size
    if (n == 0) return ResultatCalcul(gains = emptyList(), cagnotteRestante = cagnotte)

    val part = cagnotte / n   // une part = 1/n de la cagnotte
    val gains = classement.mapIndexed { index, joueur ->
        val montant = when {
            n == 1 -> cagnotte                        // seul à avoir misé : il récupère tout
            index == n - 1 -> 0                       // le dernier : rien
            index == 0 -> cagnotte - part * (n - 2)   // le premier : 2 parts + l'arrondi
            else -> part                              // le milieu : 1 part
        }
        Gain(joueur = joueur, rang = index + 1, montant = montant)
    }
    return ResultatCalcul(gains = gains, cagnotteRestante = cagnotte - gains.sumOf { it.montant })
}
