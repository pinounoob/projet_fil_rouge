package com.exemple.reveil.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.exemple.reveil.ui.theme.screens.alarme.AlarmeScreen
import com.exemple.reveil.ui.theme.screens.home.HomeScreen
import com.exemple.reveil.ui.theme.screens.nouveaugroupe.NouveauGrp

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object NouveauGroupe : Screen("nouveau_groupe")
    object Parametres : Screen("parametres")
    object Alarme : Screen("alarme/{groupeId}") {
        fun createRoute(groupeId: String) = "alarme/$groupeId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNouveauGroupeClick = {
                    navController.navigate(Screen.NouveauGroupe.route)
                },
                onParametresClick = {
                    navController.navigate(Screen.Parametres.route)
                }
            )
        }

        composable(Screen.NouveauGroupe.route) {
            NouveauGrp(
                onGroupeCree = {
                    navController.popBackStack()
                },
                onAnnuler = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Alarme.route) { backStackEntry ->
            val groupeId = backStackEntry.arguments?.getString("groupeId") ?: ""
            AlarmeScreen(
                groupeId = groupeId,
                onRetourAccueil = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}