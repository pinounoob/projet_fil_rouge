package com.exemple.reveil.navigation

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
}

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
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
    }
}