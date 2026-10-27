package com.exemple.reveil.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.exemple.reveil.ui.theme.screens.home.HomeScreen
import com.exemple.reveil.ui.theme.screens.NouveauGrp

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
                onValider = { navController.popBackStack() },
                onAnnuler = { navController.popBackStack() }
            )
        }

        //Paramètres
        composable(Routes.PARAMETRES) {
            val context = LocalContext.current
            val parametresViewModel: ParametresViewModel = viewModel(
                factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application) )
            val uiState by parametresViewModel.uiState.collectAsState()
            ParametresScreen(
                themeChoice = uiState.themeChoice,
                onThemeChange = { parametresViewModel.updateTheme(it) },
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
            arguments = listOf(navArgument("groupeId") { type = NavType.IntType })
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