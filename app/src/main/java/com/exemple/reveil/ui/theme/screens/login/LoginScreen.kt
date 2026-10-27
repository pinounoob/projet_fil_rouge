package com.exemple.reveil.ui.theme.screens.login
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.exemple.reveil.R
import com.exemple.reveil.ui.theme.ReveilTheme

import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(),
    onConnexionClick: () -> Unit) {

    val uiState by viewModel.uiState.collectAsState()

    LoginScreenContent(
        uiState = uiState,
        onEmailChange = { viewModel.onEmailChange(it) },
        onPseudoChange = { viewModel.onPseudoChange(it) },
        onMotDePasseChange = { viewModel.onMotDePasseChange(it) },
        onTogglePasswordVisibility = { viewModel.onTogglePasswordVisibility() },
        onInscriptionClick = { viewModel.sinscrire() },
        onConnexionClick = { viewModel.seConnecter(onSuccess = onConnexionClick) },
        onMdpOublieClick = { viewModel.reinitialiserMotDePasse() },
        modifier = modifier
    )
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPseudoChange: (String) -> Unit,
    onMotDePasseChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onMdpOublieClick: () -> Unit,
    onInscriptionClick: () -> Unit,
    onConnexionClick: () -> Unit,
    modifier: Modifier = Modifier) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_reveil),
            contentDescription = "Logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(250.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.size(50.dp))

        // messages Firebase
        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(300.dp)
                    .imePadding()
            )
        }

        if (uiState.successMessage != null) {
            Text(
                text = uiState.successMessage,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(300.dp)
                    .imePadding()
            )
        }

        TextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = { Text("Adresse Email") },
            singleLine = true,
            modifier = Modifier.width(300.dp)
        )

        Spacer(modifier = Modifier.size(16.dp))

        TextField(
            value = uiState.pseudo,
            onValueChange = onPseudoChange,
            label = { Text("Pseudo (inscription uniquement)") },
            singleLine = true,
            modifier = Modifier.width(300.dp)
        )
        Spacer(modifier = Modifier.size(16.dp))

        PasswordTextField(
            password = uiState.motDePasse,
            isPasswordVisible = uiState.isPasswordVisible,
            onPasswordChange = onMotDePasseChange,
            onTogglePasswordVisibility = onTogglePasswordVisibility
        )

        TextButton(
            onClick = onMdpOublieClick,
            modifier = Modifier.imePadding()
        ) {
            Text("Mot de passe oublié ?")
        }

        Spacer(modifier = Modifier.size(20.dp))

        Row {
            Button(onClick = onInscriptionClick) {
                Text("S'inscrire")
            }
            Spacer(modifier = Modifier.size(20.dp))
            Button(onClick = onConnexionClick)  {
                Text("Se connecter")
            }
        }
    }
}

@Composable
fun PasswordTextField(
    password: String,
    isPasswordVisible: Boolean,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit
) {
    TextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Mot de passe") },
        singleLine = true,
        modifier = Modifier.width(300.dp),
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onTogglePasswordVisibility) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (isPasswordVisible) "Masquer le mot de passe" else "Afficher le mot de passe"
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ReveilTheme {
        LoginScreenContent(
            uiState = LoginUiState(),
            onEmailChange = {},
            onPseudoChange = {},
            onMotDePasseChange = {},
            onTogglePasswordVisibility = {},
            onMdpOublieClick = {},
            onInscriptionClick = {},
            onConnexionClick = {}
        )
    }
}