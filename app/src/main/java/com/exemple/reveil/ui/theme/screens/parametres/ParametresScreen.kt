package com.exemple.reveil.ui.theme.screens.parametres

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.exemple.reveil.ui.theme.ReveilTheme
import com.exemple.reveil.ui.theme.screens.home.HomeScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParametresScreen(
    themeChoice: String = "SYSTEME",
    onThemeChange: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Thème de l'application", style = MaterialTheme.typography.titleMedium)
            // Boutons radios ou Dropdown pour le thème
            Row {
                RadioButton(selected = themeChoice == "CLAIR", onClick = { onThemeChange("CLAIR") })
                Text("Clair")
                RadioButton(selected = themeChoice == "SOMBRE", onClick = { onThemeChange("SOMBRE") })
                Text("Sombre")
                RadioButton(selected = themeChoice == "SYSTEME", onClick = { onThemeChange("SYSTEME") })
                Text("Système")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Se déconnecter")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ParametresScreenPreview() {
    ReveilTheme {
        ParametresScreen()
    }
}