package com.exemple.reveil.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exemple.reveil.R
import com.exemple.reveil.ui.theme.ReveilTheme

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        //Barre du haut : image, recherche
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_reveil),
                contentDescription = "Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.size(8.dp))

            TextField(
                state = rememberTextFieldState(initialText = ""),
                placeholder = { Text("Rechercher") },
                lineLimits = TextFieldLineLimits.SingleLine,
                shape = RoundedCornerShape(50),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.size(8.dp))

            FilledIconButton(onClick = { }) {
                Text("+", fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.size(40.dp))

        //Bouton du groupe
        Box(modifier = Modifier.align(Alignment.Start)) {
            var menuOuvert by remember { mutableStateOf(false) }

            Button(onClick = { menuOuvert = true }) {
                Text("Groupe Famille ▾")
            }

            DropdownMenu(
                expanded = menuOuvert,
                onDismissRequest = { menuOuvert = false }
            ) {
                DropdownMenuItem(text = { Text("Groupe Amis") }, onClick = { menuOuvert = false })
                DropdownMenuItem(text = { Text("Groupe Travail") }, onClick = { menuOuvert = false })
                DropdownMenuItem(text = { Text("Groupe Sport") }, onClick = { menuOuvert = false })
            }
        }

        //Petit espace entre le groupe et la cagnotte
        Spacer(modifier = Modifier.size(30.dp))

        //Cagnotte
        Column(horizontalAlignment = Alignment.Start) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Cagnotte", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.size(6.dp))
                FilledIconButton(onClick = { }, modifier = Modifier.size(20.dp)) {
                    Text("+", fontSize = 12.sp)
                }
            }
            Text(text = "150 €", fontSize = 48.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.size(130.dp))

        //Alarme
        Column(horizontalAlignment = Alignment.Start) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Alarme", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.size(6.dp))
                FilledIconButton(onClick = { }, modifier = Modifier.size(20.dp)) {
                    Text("+", fontSize = 12.sp)
                }
            }
            Text(text = "⏰ 07:00", fontSize = 36.sp)
        }

        //Espace entre l'alarme et les contacts
        Spacer(modifier = Modifier.size(200.dp))

        //Contacts, alignés à gauche
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Contacts", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.size(6.dp))
                FilledIconButton(onClick = { }, modifier = Modifier.size(20.dp)) {
                    Text("+", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.size(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Marie", fontSize = 18.sp)
                Text("Lucas", fontSize = 18.sp)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ReveilTheme {
        HomeScreen()
    }
}