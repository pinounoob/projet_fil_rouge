package com.exemple.reveil

import com.exemple.reveil.calculerGains
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculGainsTest {

    //4 joueurs
    @Test
    fun quatreJoueurs() {
        val resultat = calculerGains(listOf("Inès", "Aliona", "Kaouthar", "Lucas"), cagnotte = 150)

        assertEquals(listOf(5, 3, 2, 0), resultat.gains.map { it.montant })
        assertEquals(listOf(1, 2, 3, 4), resultat.gains.map { it.rang })
        assertEquals(140, resultat.cagnotteRestante)
    }

    //groupe de 2 joueurs alors le troisème gains reste dans la cagnotte
    @Test
    fun deuxJoueurs() {
        val resultat = calculerGains(listOf("Inès", "Aliona"), cagnotte = 150)

        assertEquals(listOf(5, 3), resultat.gains.map { it.montant })
        assertEquals(142, resultat.cagnotteRestante)
    }

    //si la cagnotte est insuffisante alors on donne ce qu'il reste
    @Test
    fun cagnotteInsuffisante() {
        val resultat = calculerGains(listOf("Inès", "Aliona", "Kaouthar"), cagnotte = 6)

        assertEquals(listOf(5, 1, 0), resultat.gains.map { it.montant })
        assertEquals(0, resultat.cagnotteRestante)
    }

    //si cagnotte vide, elle reste vide
    @Test
    fun cagnotteVide() {
        val resultat = calculerGains(listOf("Inès", "Aliona"), cagnotte = 0)

        assertEquals(listOf(0, 0), resultat.gains.map { it.montant })
        assertEquals(0, resultat.cagnotteRestante)
    }

    //si personne ne se lève alors la cagnotte ne bouge pas
    @Test
    fun personneLeve() {
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