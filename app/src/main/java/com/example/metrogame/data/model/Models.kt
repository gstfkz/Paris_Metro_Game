package com.example.metrogame.data.model

/** Les deux modes de transport pris en compte par le jeu. */
enum class TransportMode {
    METRO,
    RER
}

/**
 * Une ligne du réseau (ex: "1", "3bis", "A"...).
 * [id] est l'identifiant technique Navitia (ex: "line:IDFM:C01371"), utilisé pour les
 * comparaisons ; [code] est le libellé affiché à l'utilisateur (ex: "1", "3bis", "A").
 */
data class MetroLine(
    val id: String,
    val code: String,
    val name: String,
    val mode: TransportMode,
    val colorHex: String?
) {
    val displayName: String
        get() = if (mode == TransportMode.RER) "RER $code" else "Métro $code"
}

/** Une station (stop_area Navitia). */
data class MetroStation(
    val id: String,
    val name: String
)

/**
 * Réseau complet chargé depuis l'API : pour chaque ligne, la liste de ses stations.
 * Pour le RER, [stationsByLine] ne contient déjà que les stations intra-muros
 * (filtrage fait dans le repository).
 */
data class MetroNetwork(
    val lines: List<MetroLine>,
    val stationsByLine: Map<String, List<MetroStation>> // clé = MetroLine.id
)

/**
 * Une des lignes de saisie de l'utilisateur (un des blocs "ligne / station départ / station
 * arrivée" affichés à l'écran, potentiellement dupliqué via le bouton "+").
 */
data class UserSegment(
    val line: MetroLine? = null,
    val departureStation: MetroStation? = null,
    val arrivalStation: MetroStation? = null
) {
    val isComplete: Boolean
        get() = line != null && departureStation != null && arrivalStation != null
}

/** Un tronçon de l'itinéraire officiel renvoyé par l'API PRIM, une fois filtré/parsé. */
data class OfficialSegment(
    val lineId: String,
    val lineCode: String,
    val mode: TransportMode,
    val fromStationId: String,
    val fromStationName: String,
    val toStationId: String,
    val toStationName: String,
    val durationSeconds: Int
)

/** Itinéraire officiel complet (le plus rapide en temps, filtré métro+RER uniquement). */
data class OfficialJourney(
    val segments: List<OfficialSegment>,
    val totalDurationSeconds: Int
)

/** Écrans de l'application. */
sealed class GameScreen {
    data object Menu : GameScreen()
    data object Loading : GameScreen()
    data class Playing(
        val departureStation: MetroStation,
        val arrivalStation: MetroStation,
        val segments: List<UserSegment>
    ) : GameScreen()
    data class Result(
        val won: Boolean,
        val userSegments: List<UserSegment>,
        val officialJourney: OfficialJourney
    ) : GameScreen()
    data class Error(val message: String) : GameScreen()
}
