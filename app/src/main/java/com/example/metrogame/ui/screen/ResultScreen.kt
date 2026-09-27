package com.example.metrogame.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.metrogame.data.model.OfficialJourney
import com.example.metrogame.data.model.UserSegment

@Composable
fun ResultScreen(
    won: Boolean,
    userSegments: List<UserSegment>,
    officialJourney: OfficialJourney,
    onReplay: () -> Unit,
    onQuit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            if (won) "Gagné !" else "Perdu",
            style = MaterialTheme.typography.headlineMedium
        )

        if (!won) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text("Ton itinéraire :", style = MaterialTheme.typography.titleSmall)
            userSegments.filter { it.isComplete }.forEach { seg ->
                Text("${seg.line?.displayName} : ${seg.departureStation?.name} → ${seg.arrivalStation?.name}")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text("Itinéraire officiel (le plus rapide) :", style = MaterialTheme.typography.titleSmall)
            officialJourney.segments.forEach { seg ->
                Text("${seg.mode.name} ${seg.lineCode} : ${seg.fromStationName} → ${seg.toStationName}")
            }
            Text(
                "Durée totale : ${officialJourney.totalDurationSeconds / 60} min",
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onReplay) { Text("Rejouer") }
            Button(onClick = onQuit) { Text("Quitter") }
        }
    }
}
