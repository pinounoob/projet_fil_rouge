package com.exemple.reveil.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.exemple.reveil.ui.screens.alarme.AlarmeScreen
import com.exemple.reveil.ui.screens.alarme.AlarmeViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.exemple.reveil.ui.screens.login.LoginScreen
import com.exemple.reveil.ui.screens.nouveaugroupe.NouveauGrp
import com.exemple.reveil.ui.screens.nouveaugroupe.NouveauGrpViewModel
import com.exemple.reveil.ui.screens.home.HomeScreen
import com.exemple.reveil.ui.screens.home.HomeViewModel
import com.exemple.reveil.ui.screens.parametres.ParametresScreen
import com.exemple.reveil.ui.screens.parametres.ParametresViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import android.app.Application
import com.exemple.reveil.ui.screens.login.LoginViewModel

//les noms de nos écrans
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val NOUVEAU_GROUPE = "nouveau_groupe"
    const val PARAMETRES = "parametres"
    const val ALARME = "alarme/{groupeId}"

    fun alarme(groupeId: String) = "alarme/$groupeId"
}

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Connexion
        composable(Routes.LOGIN) {
            val context = LocalContext.current
            val loginViewModel: LoginViewModel = viewModel(
                factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
            )

            LoginScreen(
                viewModel = loginViewModel,
                onConnexionClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        //Accueil
        composable(Routes.HOME) {
            val context = LocalContext.current
            val homeViewModel: HomeViewModel = viewModel(
                factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
            )
            HomeScreen(
                viewModel = homeViewModel,
                onNouveauGroupe = { navController.navigate(Routes.NOUVEAU_GROUPE) },
                onParametres = { navController.navigate(Routes.PARAMETRES) }
            )
        }

        //Nouveau groupe
        composable(Routes.NOUVEAU_GROUPE) {
            val nouveauGrpViewModel: NouveauGrpViewModel = viewModel()
            NouveauGrp(
                viewModel = nouveauGrpViewModel,
                onGroupeCree = { navController.popBackStack() },  // retour à l'accueil
                onAnnuler = { navController.popBackStack() }
            )
        }

        //Paramètres
        composable(Routes.PARAMETRES) {
            val context = LocalContext.current
            val parametresViewModel: ParametresViewModel = viewModel(
                factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application) )
            ParametresScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    parametresViewModel.clearSession()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        //Alarme qui sonne
        composable(
            route = Routes.ALARME,
            arguments = listOf(navArgument("groupeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupeId = backStackEntry.arguments?.getString("groupeId") ?: ""
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