package com.exemple.reveil.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import com.exemple.reveil.ui.theme.screens.alarme.AlarmeScreen
import com.exemple.reveil.ui.theme.screens.alarme.AlarmeViewModel
import com.exemple.reveil.ui.theme.screens.login.LoginScreen
import com.exemple.reveil.ui.theme.screens.nouveaugroupe.NouveauGrp
import com.exemple.reveil.ui.theme.screens.nouveaugroupe.NouveauGrpViewModel
import com.exemple.reveil.ui.theme.screens.home.HomeScreen
import com.exemple.reveil.ui.theme.screens.NouveauGrp

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
    startDestination: String = Screen.Login.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.NouveauGroupe.route) {
            val nouveauGrpViewModel: NouveauGrpViewModel = viewModel()
            NouveauGrp(
                viewModel = nouveauGrpViewModel,
                onGroupeCree = {
                    navController.popBackStack()
                },
                onAnnuler = {
                    navController.popBackStack()
                }
            )
        }

        // Écran des Paramètres
        composable(Screen.Parametres.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Écran des paramètres")
            }
        }

        // Écran de l'Alarme
        composable(
            route = Screen.Alarme.route,
            arguments = listOf(
                navArgument("groupeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupeId = backStackEntry.arguments?.getString("groupeId") ?: ""
            val alarmeViewModel: AlarmeViewModel = viewModel()

            AlarmeScreen(
                groupeId = groupeId,
                viewModel = alarmeViewModel,
                onRetourAccueil = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
