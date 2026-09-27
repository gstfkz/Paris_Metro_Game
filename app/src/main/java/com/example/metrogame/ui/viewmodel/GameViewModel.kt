package com.example.metrogame.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.metrogame.data.model.GameScreen
import com.example.metrogame.data.model.MetroLine
import com.example.metrogame.data.model.MetroNetwork
import com.example.metrogame.data.model.MetroStation
import com.example.metrogame.data.model.UserSegment
import com.example.metrogame.data.repository.MetroRepository
import com.example.metrogame.util.ItineraryComparator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(private val repository: MetroRepository) : ViewModel() {

    private val _screen = MutableStateFlow<GameScreen>(GameScreen.Menu)
    val screen: StateFlow<GameScreen> = _screen.asStateFlow()

    private var network: MetroNetwork? = null

    /** Lignes disponibles pour le 1er menu déroulant (une fois le réseau chargé). */
    val availableLines: List<MetroLine>
        get() = network?.lines.orEmpty()

    /** Stations disponibles pour une ligne donnée (2e et 3e menus). */
    fun stationsForLine(line: MetroLine): List<MetroStation> =
        network?.stationsByLine?.get(line.id).orEmpty()

    fun startNewGame() {
        _screen.value = GameScreen.Loading
        viewModelScope.launch {
            try {
                val net = network ?: repository.loadNetwork().also { network = it }
                val (from, to) = repository.pickRandomStationPair(net)
                _screen.value = GameScreen.Playing(
                    departureStation = from,
                    arrivalStation = to,
                    segments = listOf(UserSegment())
                )
            } catch (e: Exception) {
                _screen.value = GameScreen.Error(
                    e.message ?: "Impossible de charger le réseau métro/RER."
                )
            }
        }
    }

    fun updateSegment(index: Int, updated: UserSegment) {
        val current = _screen.value as? GameScreen.Playing ?: return
        val newSegments = current.segments.toMutableList().apply { this[index] = updated }
        _screen.value = current.copy(segments = newSegments)
    }

    /** N'a d'effet que si le dernier bloc est complet (cf. règles de l'énoncé). */
    fun addSegmentRow() {
        val current = _screen.value as? GameScreen.Playing ?: return
        if (current.segments.lastOrNull()?.isComplete != true) return
        _screen.value = current.copy(segments = current.segments + UserSegment())
    }

    fun removeSegmentRow(index: Int) {
        val current = _screen.value as? GameScreen.Playing ?: return
        if (current.segments.size <= 1) return
        _screen.value = current.copy(segments = current.segments.toMutableList().apply { removeAt(index) })
    }

    /** Appelle l'API PRIM en direct pour obtenir l'itinéraire officiel et compare. */
    fun verify() {
        val current = _screen.value as? GameScreen.Playing ?: return
        if (current.segments.lastOrNull()?.isComplete != true) return

        _screen.value = GameScreen.Loading
        viewModelScope.launch {
            try {
                val official = repository.fetchOfficialJourney(
                    current.departureStation.id,
                    current.arrivalStation.id
                )
                if (official == null) {
                    _screen.value = GameScreen.Error(
                        "Aucun itinéraire uniquement métro/RER trouvé pour cette paire de " +
                            "stations. Lance une nouvelle partie."
                    )
                    return@launch
                }
                val won = ItineraryComparator.matches(current.segments, official)
                _screen.value = GameScreen.Result(
                    won = won,
                    userSegments = current.segments,
                    officialJourney = official
                )
            } catch (e: Exception) {
                _screen.value = GameScreen.Error(
                    e.message ?: "Erreur réseau lors de la vérification de l'itinéraire."
                )
            }
        }
    }

    fun backToMenu() {
        _screen.value = GameScreen.Menu
    }

    class Factory(private val repository: MetroRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GameViewModel(repository) as T
    }
}
