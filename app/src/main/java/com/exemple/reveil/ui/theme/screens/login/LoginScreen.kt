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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun LoginScreen(modifier: Modifier = Modifier,
                viewModel: LoginViewModel = viewModel(),
                onConnexionClick: () -> Unit) {

    val uiState by viewModel.uiState.collectAsState()
    LoginScreenContent(
        uiState = uiState,
        onIdentifiantChange = { viewModel.onIdentifiantChange(it) },
        onMotDePasseChange = { viewModel.onMotDePasseChange(it) },
        onTogglePasswordVisibility = { viewModel.onTogglePasswordVisibility() },
        onConnexionClick = {
            viewModel.login()
            onConnexionClick()
        },
        modifier = modifier
    )
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onIdentifiantChange: (String) -> Unit,
    onMotDePasseChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
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

        TextField(
            value = uiState.identifiant,
            onValueChange = onIdentifiantChange,
            label = { Text("Identifiant") },
            singleLine = true,
            modifier = Modifier.width(300.dp)
        )
        Spacer(modifier = Modifier.size(32.dp))

        PasswordTextField(
            password = uiState.motDePasse,
            isPasswordVisible = uiState.isPasswordVisible,
            onPasswordChange = onMotDePasseChange,
            onTogglePasswordVisibility = onTogglePasswordVisibility
        )

        Spacer(modifier = Modifier.size(50.dp))

        Row {
            Button(onClick = { /* Logique d'inscription plus tard */ }) {
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
            uiState = LoginUiState(identifiant = ""),
            onIdentifiantChange = {},
            onMotDePasseChange = {},
            onTogglePasswordVisibility = {},
            onConnexionClick = {}
        )
    }
}