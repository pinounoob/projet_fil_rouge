package com.exemple.reveil.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.exemple.reveil.ui.theme.screens.home.HomeScreen
import com.exemple.reveil.ui.theme.screens.NouveauGrp
import com.exemple.reveil.ui.theme.screens.login.LoginScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object NouveauGroupe : Screen("nouveau_groupe")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route, // 👈 Démarre ici !
        modifier = modifier
    ) {
        // Écran de Connexion
        composable(Screen.Login.route) {
            LoginScreen(
                onConnexionClick = {
                    // Navigation vers l'accueil après connexion en nettoyant la pile
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Écran d'Accueil
        composable(Screen.Home.route) {
            HomeScreen(
                onNouveauGroupe = {
                    navController.navigate(Screen.NouveauGroupe.route)
                }
            )
        }

        // Écran Nouveau Groupe
        composable(Screen.NouveauGroupe.route) {
            NouveauGrp(
                onValider = { navController.popBackStack() },
                onAnnuler = { navController.popBackStack() }
            )
        }
    }
}
