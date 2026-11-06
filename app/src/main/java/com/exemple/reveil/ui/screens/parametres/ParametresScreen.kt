package com.exemple.reveil.ui.screens.parametres

import com.exemple.reveil.R
import androidx.compose.ui.res.stringResource
import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Sélecteur de sonnerie natif d'Android
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (uri != null) {
                viewModel.updateRingtoneUri(uri.toString())
            }
        }
    }

    val titreSelecteur = stringResource(R.string.selectionner_sonnerie)
    val openRingtonePicker = {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, titreSelecteur)
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uiState.ringtoneUri?.let { Uri.parse(it) })
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
        }
        ringtonePickerLauncher.launch(intent)
    }

    val sonnerieParDefaut = stringResource(R.string.sonnerie_par_defaut)
    val nomSonnerie = remember(uiState.ringtoneUri) {
        val uri = uiState.ringtoneUri?.let { Uri.parse(it) }
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        RingtoneManager.getRingtone(context, uri)?.getTitle(context) ?: sonnerieParDefaut
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.parametres)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.retour))
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
            Text(stringResource(R.string.theme_application), style = MaterialTheme.typography.titleMedium)
            Row {
                RadioButton(
                    selected = uiState.themeChoice == "CLAIR",
                    onClick = { viewModel.updateTheme("CLAIR") })
                Text(stringResource(R.string.theme_clair), modifier = Modifier.align(Alignment.CenterVertically))
                RadioButton(
                    selected = uiState.themeChoice == "SOMBRE",
                    onClick = { viewModel.updateTheme("SOMBRE") })
                Text(stringResource(R.string.theme_sombre), modifier = Modifier.align(Alignment.CenterVertically))
                RadioButton(
                    selected = uiState.themeChoice == "SYSTEME",
                    onClick = { viewModel.updateTheme("SYSTEME") })
                Text(stringResource(R.string.theme_systeme), modifier = Modifier.align(Alignment.CenterVertically))
            }

            Spacer(modifier = Modifier.padding(10.dp))

            // Ligne Solde & Ajout d'argent
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.solde_montant, uiState.solde),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { viewModel.setShowMoneyDialog(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.ajouter_argent))
                }
            }

            Spacer(modifier = Modifier.padding(20.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = openRingtonePicker,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.choisir_sonnerie))
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = { viewModel.toggleRingtoneMode() }) {
                        Icon(
                            imageVector = if (uiState.ringtoneMode == "SONNERIE") Icons.Filled.Notifications else Icons.Filled.Vibration,
                            contentDescription = stringResource(R.string.basculer_mode_sonnerie),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.sonnerie_actuelle, nomSonnerie, uiState.ringtoneMode.lowercase()),
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
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
                Text(stringResource(R.string.parametres_compte))
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text(stringResource(R.string.se_deconnecter))
            }
        }
    }

    // Popup ARGENT
    if (uiState.showMoneyDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowMoneyDialog(false) },
            title = { Text(stringResource(R.string.ajouter_argent)) },
            text = {
                OutlinedTextField(
                    value = uiState.moneyInput,
                    onValueChange = { viewModel.updateMoneyInput(it) },
                    label = { Text(stringResource(R.string.montant_champ)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.validerAjoutArgent() }) { Text(stringResource(R.string.valider)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowMoneyDialog(false) }) { Text(stringResource(R.string.annuler)) }
            }
        )
    }

    // Popup COMPTE
    if (uiState.showAccountDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowAccountDialog(false) },
            title = { Text(stringResource(R.string.modifier_compte)) },
            text = {
                Column {
                    if (uiState.accountErrorMessage != null) {
                        Text(
                            text = uiState.accountErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = uiState.newPseudoInput,
                        onValueChange = { viewModel.updateNewPseudoInput(it) },
                        label = { Text(stringResource(R.string.nouveau_pseudo)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.newPasswordInput,
                        onValueChange = { viewModel.updateNewPasswordInput(it) },
                        label = { Text(stringResource(R.string.nouveau_mot_de_passe)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.validerModificationsCompte() }) { Text(stringResource(R.string.enregistrer)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowAccountDialog(false) }) { Text(stringResource(R.string.annuler)) }
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
