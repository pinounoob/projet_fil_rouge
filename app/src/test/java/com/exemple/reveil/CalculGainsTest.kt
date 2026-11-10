package com.exemple.reveil

import com.exemple.reveil.logique.calculerGains
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculGainsTest {

    //2 joueurs : le premier gagne toute la cagnotte, le dernier rien
    @Test
    fun deuxJoueurs() {
        val resultat = calculerGains(listOf("Inès", "Aliona"), cagnotte = 20)

        assertEquals(listOf(20, 0), resultat.gains.map { it.montant })
        assertEquals(0, resultat.cagnotteRestante)
    }

    //3 joueurs : le premier gagne plus que sa mise, le deuxième récupère sa mise, le troisième rien
    @Test
    fun troisJoueurs() {
        val resultat = calculerGains(listOf("Inès", "Aliona", "Kaouthar"), cagnotte = 30)

        assertEquals(listOf(20, 10, 0), resultat.gains.map { it.montant })
        assertEquals(listOf(1, 2, 3), resultat.gains.map { it.rang })
        assertEquals(0, resultat.cagnotteRestante)
    }

    //4 joueurs : 2 parts / 1 part / 1 part / 0
    @Test
    fun quatreJoueurs() {
        val resultat = calculerGains(listOf("Inès", "Aliona", "Kaouthar", "Lucas"), cagnotte = 40)

        assertEquals(listOf(20, 10, 10, 0), resultat.gains.map { it.montant })
        assertEquals(0, resultat.cagnotteRestante)
    }

    //l'arrondi de la division va au premier, rien ne se perd
    @Test
    fun arrondiAuPremier() {
        val resultat = calculerGains(listOf("Inès", "Aliona", "Kaouthar"), cagnotte = 31)

        assertEquals(listOf(21, 10, 0), resultat.gains.map { it.montant })
        assertEquals(0, resultat.cagnotteRestante)
    }

    //un seul joueur a misé : il récupère sa mise
    @Test
    fun seulJoueur() {
        val resultat = calculerGains(listOf("Inès"), cagnotte = 10)

        assertEquals(listOf(10), resultat.gains.map { it.montant })
        assertEquals(0, resultat.cagnotteRestante)
    }

    //si personne n'a misé alors la cagnotte ne bouge pas
    @Test
    fun personne() {
        val resultat = calculerGains(emptyList(), cagnotte = 150)

        assertEquals(0, resultat.gains.size)
        assertEquals(150, resultat.cagnotteRestante)
    }

    //doit lever une exception
    @Test(expected = IllegalArgumentException::class)
    fun cagnotteNegative() {
        calculerGains(listOf("Inès"), cagnotte = -1)
    }
}
