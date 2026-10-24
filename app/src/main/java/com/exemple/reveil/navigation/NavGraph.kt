package com.exemple.reveil.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.exemple.reveil.ui.theme.screens.home.HomeScreen
import com.exemple.reveil.ui.theme.screens.NouveauGrp

sealed class Screen(val route: String) {
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
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.NouveauGroupe.route) {
            NouveauGrp(
                onValider = { navController.popBackStack() },
                onAnnuler = { navController.popBackStack() }
            )
        }
    }
}