package com.example.metrogame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.metrogame.data.model.GameScreen
import com.example.metrogame.data.network.PrimApiClient
import com.example.metrogame.data.repository.MetroRepository
import com.example.metrogame.ui.screen.GameScreen as GameScreenView
import com.example.metrogame.ui.screen.MenuScreen
import com.example.metrogame.ui.screen.ResultScreen
import com.example.metrogame.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    // Assemblage manuel des dépendances (pas d'injection de dépendances pour ce petit projet).
    private val repository by lazy {
        MetroRepository(PrimApiClient.create(BuildConfig.PRIM_API_KEY))
    }
    private val viewModel: GameViewModel by viewModels { GameViewModel.Factory(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val screen by viewModel.screen.collectAsState()

                    when (val s = screen) {
                        is GameScreen.Menu -> MenuScreen(
                            onPlay = { viewModel.startNewGame() },
                            onQuit = { finish() }
                        )

                        is GameScreen.Loading -> LoadingView()

                        is GameScreen.Playing -> GameScreenView(
                            departureStation = s.departureStation,
                            arrivalStation = s.arrivalStation,
                            segments = s.segments,
                            availableLines = viewModel.availableLines,
                            stationsForLine = { viewModel.stationsForLine(it) },
                            onSegmentChange = viewModel::updateSegment,
                            onAddRow = viewModel::addSegmentRow,
                            onRemoveRow = viewModel::removeSegmentRow,
                            onVerify = viewModel::verify
                        )

                        is GameScreen.Result -> ResultScreen(
                            won = s.won,
                            userSegments = s.userSegments,
                            officialJourney = s.officialJourney,
                            onReplay = { viewModel.startNewGame() },
                            onQuit = { finish() }
                        )

                        is GameScreen.Error -> ErrorView(
                            message = s.message,
                            onRetry = { viewModel.backToMenu() }
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun LoadingView() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Text("Chargement...", modifier = Modifier.padding(top = 16.dp))
    }
}

@androidx.compose.runtime.Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, modifier = Modifier.padding(bottom = 16.dp))
        Button(onClick = onRetry) { Text("Retour au menu") }
    }
}
