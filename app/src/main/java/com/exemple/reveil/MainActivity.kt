package com.exemple.reveil

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
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
import com.exemple.reveil.ui.theme.ReveilTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")
        enableEdgeToEdge()
        setContent {
            ReveilTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Aliona",
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Column (modifier = Modifier.padding(24.dp).fillMaxWidth()) {
//        Text(text = "Hello $name!", modifier=modifier)
//        Text(text = "Saluuut !!")
//        Image(
//            painter = painterResource(id = R.drawable.image_roumanie),
//            contentDescription = "")
//    }


    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
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
            state = rememberTextFieldState(initialText = ""),
            label = { Text("Identifiant") },
            lineLimits = TextFieldLineLimits.SingleLine
        )

        Spacer(modifier = Modifier.size(32.dp))

        TextField(
            state = rememberTextFieldState(initialText = ""),
            label = { Text("Mot de passe") },
            lineLimits = TextFieldLineLimits.SingleLine
        )

        Spacer(modifier = Modifier.size(50.dp))

        Row() {
            Button(onClick = { }) {
                Text("S'inscrire")
            }
            Spacer(modifier = Modifier.size(20.dp))

            Button(onClick = { }) {
                Text("Se connecter")
            }
        }

    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ReveilTheme {
        Greeting("Aliona")
    }
}
