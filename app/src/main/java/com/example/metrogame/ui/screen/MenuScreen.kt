package com.example.metrogame.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MenuScreen(
    onPlay: () -> Unit,
    onQuit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Metro Game", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Retrouve l'itinéraire le plus rapide entre 2 stations",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        Button(onClick = onPlay, modifier = Modifier.padding(bottom = 12.dp)) {
            Text("Jouer")
        }
        Button(onClick = onQuit) {
            Text("Quitter")
        }
    }
}
