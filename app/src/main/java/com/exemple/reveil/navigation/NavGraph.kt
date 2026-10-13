package com.exemple.reveil.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.exemple.reveil.ui.theme.screens.alarme.AlarmeScreen
import com.exemple.reveil.ui.theme.screens.alarme.AlarmeViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.exemple.reveil.ui.theme.screens.login.LoginScreen
import com.exemple.reveil.ui.theme.screens.NouveauGrp
import com.exemple.reveil.ui.theme.screens.NouveauGrpViewModel
import com.exemple.reveil.ui.theme.screens.home.HomeScreen
import com.exemple.reveil.ui.theme.screens.home.HomeViewModel

//les noms de nos écrans
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val NOUVEAU_GROUPE = "nouveau_groupe"
    const val ALARME = "alarme/{groupeId}"

    fun alarme(groupeId: Int) = "alarme/$groupeId"
}

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "alarme/1"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        //Connexion
        composable(Routes.LOGIN) {
            LoginScreen(
                onConnexionClick = {
                    navController.navigate(Routes.HOME) {
                        //retire le login de la pile : le retour ne ramène pas à la connexion
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        //Accueil
        composable(Routes.HOME) {
            // Un ViewModel par écran, lié à cette destination
            val homeViewModel: HomeViewModel = viewModel()
            HomeScreen(
                viewModel = homeViewModel,
                onNouveauGroupe = { navController.navigate(Routes.NOUVEAU_GROUPE) }
            )
        }

        //Nouveau groupe
        composable(Routes.NOUVEAU_GROUPE) {
            val nouveauGrpViewModel: NouveauGrpViewModel = viewModel()
            NouveauGrp(
                viewModel = nouveauGrpViewModel,
                onValider = { navController.popBackStack() },  // retour à l'accueil
                onAnnuler = { navController.popBackStack() }
            )
        }

        //Alarme qui sonne
        composable(
            route = Routes.ALARME,
            arguments = listOf(navArgument("groupeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupeId = backStackEntry.arguments?.getInt("groupeId") ?: 0
            val alarmeViewModel: AlarmeViewModel = viewModel()
            AlarmeScreen(
                groupeId = groupeId,
                viewModel = alarmeViewModel,
                onRetourAccueil = {
                    // Si l'accueil est déjà dans la pile, on y revient
                    val retourOk = navController.popBackStack(Routes.HOME, inclusive = false)
                    // Sinon (appli ouverte par l'alarme), on l'ouvre et on retire l'alarme
                    if (!retourOk) {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ALARME) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}