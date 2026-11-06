package com.exemple.reveil.ui.theme.screens.parametres

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exemple.reveil.ui.theme.ReveilTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParametresScreen(
    viewModel: ParametresViewModel = viewModel(),
    onLogout: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    // Sélecteur de fichier audio
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.updateRingtoneUri(it.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (uiState.pseudo.isNotBlank()) {
                        Text(
                            text = uiState.pseudo,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {
            Text("Thème de l'application", style = MaterialTheme.typography.titleMedium)
            Row {
                RadioButton(
                    selected = uiState.themeChoice == "CLAIR",
                    onClick = { viewModel.updateTheme("CLAIR") })
                Text("Clair", modifier = Modifier.align(Alignment.CenterVertically))
                RadioButton(
                    selected = uiState.themeChoice == "SOMBRE",
                    onClick = { viewModel.updateTheme("SOMBRE") })
                Text("Sombre", modifier = Modifier.align(Alignment.CenterVertically))
                RadioButton(
                    selected = uiState.themeChoice == "SYSTEME",
                    onClick = { viewModel.updateTheme("SYSTEME") })
                Text("Système", modifier = Modifier.align(Alignment.CenterVertically))
            }

            Spacer(modifier = Modifier.padding(10.dp))

            // Ligne Solde & Ajout d'argent
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Solde : ${uiState.solde} €",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { viewModel.setShowMoneyDialog(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Ajouter de l'argent virtuel")
                }
            }

            Spacer(modifier = Modifier.padding(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { audioPickerLauncher.launch("audio/*") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Choisir une sonnerie")
                }
                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = { viewModel.toggleRingtoneMode() }) {
                    Icon(
                        imageVector = if (uiState.ringtoneMode == "SONNERIE") Icons.Filled.Notifications else Icons.Filled.Vibration,
                        contentDescription = "Basculer mode sonnerie",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier= Modifier.padding(20.dp))

            Button(
                onClick = {
                    viewModel.initAccountDialog()
                    viewModel.setShowAccountDialog(true)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Paramètres compte")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Se déconnecter")
            }
        }
    }

    // Popup ARGENT
    if (uiState.showMoneyDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowMoneyDialog(false) },
            title = { Text("Ajouter de l'argent virtuel") },
            text = {
                OutlinedTextField(
                    value = uiState.moneyInput,
                    onValueChange = { viewModel.updateMoneyInput(it) },
                    label = { Text("Montant (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.validerAjoutArgent() }) { Text("Valider") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowMoneyDialog(false) }) { Text("Annuler") }
            }
        )
    }

    // Popup COMPTE
    if (uiState.showAccountDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowAccountDialog(false) },
            title = { Text("Modifier le compte") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.newPseudoInput,
                        onValueChange = { viewModel.updateNewPseudoInput(it) },
                        label = { Text("Nouveau pseudo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.newPasswordInput,
                        onValueChange = { viewModel.updateNewPasswordInput(it) },
                        label = { Text("Nouveau mot de passe") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.validerModificationsCompte() }) { Text("Enregistrer") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowAccountDialog(false) }) { Text("Annuler") }
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ParametresScreenPreview() {
    ReveilTheme {
        ParametresScreen()
    }
}