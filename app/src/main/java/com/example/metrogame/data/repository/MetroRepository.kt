package com.example.metrogame.data.repository

import com.example.metrogame.data.model.MetroLine
import com.example.metrogame.data.model.MetroNetwork
import com.example.metrogame.data.model.MetroStation
import com.example.metrogame.data.model.OfficialJourney
import com.example.metrogame.data.model.OfficialSegment
import com.example.metrogame.data.model.TransportMode
import com.example.metrogame.data.network.PrimApiService
import com.example.metrogame.data.network.StopAreaDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MetroRepository(private val api: PrimApiService) {

    private var cachedNetwork: MetroNetwork? = null

    /**
     * Charge (une seule fois, puis cache en mémoire) les lignes métro + RER et leurs stations.
     * Pour le RER, ne conserve que les stations intra-muros (zip_code commençant par "75").
     */
    suspend fun loadNetwork(forceRefresh: Boolean = false): MetroNetwork {
        cachedNetwork?.let { if (!forceRefresh) return it }

        val metroLinesDto = api.getLines(PrimApiService.FILTER_METRO)
        val rerLinesDto = api.getLines(PrimApiService.FILTER_RER)

        val metroLines = metroLinesDto.lines.map { it.toDomain(TransportMode.METRO) }
        val rerLines = rerLinesDto.lines.map { it.toDomain(TransportMode.RER) }
        val allLines = metroLines + rerLines

        // Récupère les stations de chaque ligne en parallèle.
        val stationsByLine = coroutineScope {
            allLines.map { line ->
                async {
                    val stopAreas = api.getStopAreasForLine(line.id).stop_areas
                    val filtered = if (line.mode == TransportMode.RER) {
                        stopAreas.filter { it.isInParisIntramuros() }
                    } else {
                        stopAreas
                    }
                    line.id to filtered.map { MetroStation(it.id, it.name) }
                        .distinctBy { it.id }
                        .sortedBy { it.name }
                }
            }.associate { it.await() }
        }

        val network = MetroNetwork(allLines, stationsByLine)
        cachedNetwork = network
        return network
    }

    /** Union dédupliquée de toutes les stations en périmètre (pour le tirage aléatoire). */
    fun allStations(network: MetroNetwork): List<MetroStation> =
        network.stationsByLine.values.flatten().distinctBy { it.id }

    fun pickRandomStationPair(network: MetroNetwork): Pair<MetroStation, MetroStation> {
        val pool = allStations(network)
        require(pool.size >= 2) { "Pas assez de stations chargées." }
        var a: MetroStation
        var b: MetroStation
        do {
            a = pool.random()
            b = pool.random()
        } while (a.id == b.id)
        return a to b
    }

    /**
     * Interroge l'API pour l'itinéraire le plus rapide en temps, restreint au métro + RER.
     * Retourne null si aucun itinéraire purement métro/RER n'existe (l'appelant doit alors
     * retirer une nouvelle paire de stations).
     */
    suspend fun fetchOfficialJourney(fromStationId: String, toStationId: String): OfficialJourney? {
        val response = api.getJourneys(
            from = fromStationId,
            to = toStationId,
            forbiddenUris = PrimApiService.FORBIDDEN_MODES
        )
        val journey = response.journeys.firstOrNull() ?: return null

        // Si un tronçon "street_network" (marche) apparaît AILLEURS qu'en tout début/fin
        // (accès à la station), c'est qu'il n'existe pas de correspondance directe en rail :
        // on rejette ce trajet plutôt que de fausser la comparaison.
        val ptSections = journey.sections.filter { it.type == "public_transport" }
        if (ptSections.isEmpty()) return null
        val hasIllegalWalk = journey.sections.any {
            it.type == "street_network" &&
                it != journey.sections.first() &&
                it != journey.sections.last()
        }
        if (hasIllegalWalk) return null

        val segments = ptSections.mapNotNull { section ->
            val info = section.display_informations ?: return@mapNotNull null
            val fromArea = section.from?.stop_point?.stop_area ?: section.from?.stop_area
            val toArea = section.to?.stop_point?.stop_area ?: section.to?.stop_area
            if (fromArea == null || toArea == null) return@mapNotNull null

            val mode = if (info.physical_mode?.contains("RapidTransit", ignoreCase = true) == true) {
                TransportMode.RER
            } else {
                TransportMode.METRO
            }
            OfficialSegment(
                lineId = "", // non fourni directement par la section ; non indispensable à la comparaison
                lineCode = info.code ?: info.commercial_mode.orEmpty(),
                mode = mode,
                fromStationId = fromArea.id,
                fromStationName = fromArea.name,
                toStationId = toArea.id,
                toStationName = toArea.name,
                durationSeconds = section.duration
            )
        }
        if (segments.isEmpty()) return null

        return OfficialJourney(segments, journey.duration)
    }

    private fun StopAreaDto.isInParisIntramuros(): Boolean =
        administrative_regions.any { it.zip_code?.startsWith("75") == true }

    private fun com.example.metrogame.data.network.LineDto.toDomain(mode: TransportMode): MetroLine =
        MetroLine(
            id = id,
            code = code ?: name,
            name = name,
            mode = mode,
            colorHex = color
        )
}
