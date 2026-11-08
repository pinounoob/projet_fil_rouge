package com.exemple.reveil

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.exemple.reveil.data.PreferenceManager
import com.exemple.reveil.navigation.NavGraph
import com.exemple.reveil.navigation.Routes
import com.exemple.reveil.ui.theme.ReveilTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private val demandeNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { accordee ->
            if (!accordee) Log.w("MainActivity", "Notifications refusées")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val preferenceManager = PreferenceManager(applicationContext)

<<<<<<< HEAD
        // Détection si l'activité a été lancée par une alarme programmée.
        // Si relance normale, on exige la connexion systématique.
=======
>>>>>>> kaouthar
        val groupeId = intent?.getStringExtra("groupeId")
        val destinationInitiale = when {
            !groupeId.isNullOrEmpty() -> Routes.alarme(groupeId)
            else -> {
                FirebaseAuth.getInstance().signOut()
                Routes.LOGIN
            }
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            demandeNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val themeChoice by preferenceManager.themeFlow.collectAsState(initial = "SYSTEME")
            val isDarkTheme = when (themeChoice) {
                "SOMBRE" -> true
                "CLAIR" -> false
                else -> isSystemInDarkTheme()
            }

            ReveilTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        startDestination = destinationInitiale,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}