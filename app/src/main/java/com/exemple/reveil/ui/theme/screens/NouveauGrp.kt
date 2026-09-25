package com.exemple.reveil.ui.theme.screens

import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exemple.reveil.ui.theme.ReveilTheme

data class Ami(
    val id: Int,
    val nom: String,
    val selectionne: Boolean = false
)

@Composable
fun NouveauGrp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var nomGroupe by remember { mutableStateOf("") }
    var champOvale by remember { mutableStateOf("") }
    var alarmeActivee by remember { mutableStateOf(false) }

    var heure by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(30) }
    val heureAffichee = String.format("%02d:%02d", heure, minute)

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, h: Int, m: Int ->
                heure = h
                minute = m
            },
            heure,
            minute,
            true
        )
    }

    val listeAmis = remember {
        mutableStateListOf(
            Ami(id = 1, nom = "Aliona", selectionne = false),
            Ami(id = 2, nom = "Ines", selectionne = false)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Créer un nouveau groupe",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = nomGroupe,
                onValueChange = { nomGroupe = it },
                label = { Text("Nom du groupe") },
                placeholder = { Text("Ex: Les lève-tôt") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alarme",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.clickable(enabled = alarmeActivee) {
                            timePickerDialog.show()
                        }
                    ) {
                        Text(
                            text = heureAffichee,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (alarmeActivee) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Switch(
                        checked = alarmeActivee,
                        onCheckedChange = { alarmeActivee = it }
                    )
                }
            }

            OutlinedTextField(
                value = champOvale,
                onValueChange = { champOvale = it },
                placeholder = { Text("Ajouter des amis") },
                shape = RoundedCornerShape(50),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listeAmis.forEachIndexed { index, ami ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val nouvelEtat = !ami.selectionne
                                listeAmis[index] = ami.copy(selectionne = nouvelEtat)
                                Log.d("CHECKBOXES", "${ami.nom} sélectionné: $nouvelEtat")
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = ami.selectionne,
                            onCheckedChange = { isChecked ->
                                listeAmis[index] = ami.copy(selectionne = isChecked)
                                Log.d("CHECKBOXES", "${ami.nom} sélectionné: $isChecked")
                            }
                        )
                        Text(
                            text = ami.nom,
                            fontSize = 17.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        val membresChoisis = listeAmis.filter { it.selectionne }.map { it.nom }
                        Log.d("GROUPE", "Création groupe '$nomGroupe' avec : $membresChoisis à $heureAffichee")
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("✓", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("✕", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NouveauGrpPreview() {
    ReveilTheme {
        NouveauGrp()
    }
}