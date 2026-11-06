package com.exemple.reveil

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.exemple.reveil.navigation.Screen
import com.exemple.reveil.ui.theme.ReveilTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceManager = PreferenceManager(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val groupeIdAlarme = intent.getStringExtra("groupeId")
        val destinationInitiale = if (!groupeIdAlarme.isNullOrEmpty()) {
            Screen.Alarme.createRoute(groupeIdAlarme)
        } else {
            Screen.Home.route
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

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy")
    }
}