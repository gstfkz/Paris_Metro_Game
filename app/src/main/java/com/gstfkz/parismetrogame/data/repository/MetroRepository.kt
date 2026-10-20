package com.gstfkz.parismetrogame.data.repository

import com.gstfkz.parismetrogame.data.model.*
import com.gstfkz.parismetrogame.data.network.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MetroRepository(private val api: PrimApiService) {
    private var cachedNetwork: MetroNetwork? = null

    suspend fun loadNetwork(forceRefresh: Boolean = false): MetroNetwork {
        cachedNetwork?.let { if (!forceRefresh) return it }
        val metro = api.getLines(PrimApiService.FILTER_METRO).lines.map { it.toDomain(TransportMode.METRO) }
        val rer = api.getLines(PrimApiService.FILTER_RER).lines.map { it.toDomain(TransportMode.RER) }
        val all = metro + rer
        val stations = coroutineScope {
            all.map { line -> async {
                val raw = api.getStopAreasForLine(line.id).stop_areas
                val filtered = if (line.mode == TransportMode.RER) raw.filter { it.isInParis() } else raw
                line.id to filtered.map { MetroStation(it.id, it.name, it.coord?.lat, it.coord?.lon) }
                    .distinctBy { it.id }.sortedBy { it.name.lowercase() }
            } }.associate { it.await() }
        }
        return MetroNetwork(all, stations).also { cachedNetwork = it }
    }

    fun allStations(n: MetroNetwork, rerEnabled: Boolean = true): List<MetroStation> {
        val ids = n.lines.filter { it.mode == TransportMode.METRO || (rerEnabled && it.mode == TransportMode.RER) }.map { it.id }.toSet()
        return n.stationsByLine.filterKeys { it in ids }.values.flatten().distinctBy { it.id }.sortedBy { it.name.lowercase() }
    }

    fun pickRandomStationPair(n: MetroNetwork, rerEnabled: Boolean): Pair<MetroStation, MetroStation> {
        val stations = allStations(n, rerEnabled)
        require(stations.size >= 2)
        val a = stations.random()
        var b: MetroStation
        do { b = stations.random() } while (a.id == b.id)
        return a to b
    }

    private fun areaId(p: SectionPointDto?): String? =
        p?.stop_point?.stop_area?.id ?: p?.stop_area?.id ?: p?.id

    private fun areaName(p: SectionPointDto?): String? =
        p?.stop_point?.stop_area?.name ?: p?.stop_area?.name ?: p?.name

    private fun sameStation(a: SectionPointDto?, b: SectionPointDto?): Boolean {
        val ai = areaId(a)
        val bi = areaId(b)
        if (ai != null && bi != null && ai == bi) return true
        val an = areaName(a)?.trim()
        val bn = areaName(b)?.trim()
        return !an.isNullOrEmpty() && !bn.isNullOrEmpty() && an.equals(bn, ignoreCase = true)
    }

    suspend fun fetchOfficialJourneys(
        from: String, to: String, walkEnabled: Boolean, rerEnabled: Boolean,
        realTrafficEnabled: Boolean = false
    ): List<OfficialJourney> {
        val forbidden = PrimApiService.BASE_FORBIDDEN_MODES.toMutableList().apply {
            if (!rerEnabled) add("physical_mode:RapidTransit")
        }
        val raw = api.getJourneys(from, to, forbidden, 20,
            dataFreshness = if (realTrafficEnabled) "realtime" else "base_schedule").journeys
        val candidates = raw.filterNot { j ->
            j.tags.any { it.equals("next", true) || it.equals("previous", true) }
        }.ifEmpty { raw }

        val parsed = candidates.mapNotNull { journey ->
            val transport = journey.sections.filter { it.type == "public_transport" }
            if (transport.isEmpty()) return@mapNotNull null
            // Never silently drop a public-transport leg, even when the mode is forbidden.
            if (transport.any { it.display_informations == null ||
                (!rerEnabled && (it.display_informations?.physical_mode.orEmpty().contains("RapidTransit", true) ||
                    it.display_informations?.commercial_mode.orEmpty().contains("RER", true))) }) return@mapNotNull null

            // An itinerary without walking must stay at the SAME stop area between
            // successive transit legs. A walking transfer to another station is invalid.
            if (!walkEnabled) {
                if (journey.sections.any { s ->
                    s.type in setOf("street_network", "crow_fly", "transfer") &&
                        !sameStation(s.from, s.to)
                }) return@mapNotNull null
                if (transport.zipWithNext().any { (a, b) -> !sameStation(a.to, b.from) })
                    return@mapNotNull null
            }
            if (!sameStation(transport.first().from, SectionPointDto(id = from)) &&
                areaId(transport.first().from) != from) {
                // The requested stop-area may be represented by a stop point: only reject
                // when an explicit first-mile walking leg is present and walking is disabled.
                if (!walkEnabled && journey.sections.takeWhile { it !== transport.first() }
                        .any { it.type in setOf("street_network", "crow_fly") && !sameStation(it.from, it.to) })
                    return@mapNotNull null
            }
            val segments = journey.sections.mapNotNull { s ->
                val a = areaId(s.from) ?: return@mapNotNull null
                val b = areaId(s.to) ?: return@mapNotNull null
                val an = areaName(s.from) ?: a
                val bn = areaName(s.to) ?: b
                when (s.type) {
                    "public_transport" -> {
                        val info = s.display_informations ?: return@mapNotNull null
                        val mode = if (info.physical_mode.orEmpty().contains("RapidTransit", true) ||
                            info.commercial_mode.orEmpty().contains("RER", true)) TransportMode.RER else TransportMode.METRO
                        OfficialSegment("", info.code ?: info.commercial_mode.orEmpty(), mode, a, an, b, bn, s.duration, info.color)
                    }
                    "street_network", "crow_fly" ->
                        if (walkEnabled && !sameStation(s.from, s.to))
                            OfficialSegment("walk", "Walk", TransportMode.WALK, a, an, b, bn, s.duration)
                        else null
                    else -> null
                }
            }
            if (segments.isEmpty() || segments.last().mode == TransportMode.WALK)
                return@mapNotNull null
            // Do not accept an itinerary that terminates at a different station.
            val finalStop = transport.last().to
            val lastArea = areaId(finalStop)
            if (lastArea != to) {
                // Navitia sometimes exposes a stop point instead of the stop area;
                // never treat an interstation final walk as an arrival at the target.
                if (journey.sections.dropWhile { it !== transport.last() }.drop(1)
                        .any { it.type in setOf("street_network", "crow_fly") && !sameStation(it.from, it.to) })
                    return@mapNotNull null
            }
            OfficialJourney(segments, journey.duration)
        }
        val shortest = parsed.minOfOrNull { it.totalDurationSeconds } ?: return emptyList()
        return parsed.filter { it.totalDurationSeconds == shortest }
            .distinctBy { it.segments.map { s -> listOf(s.mode, s.lineCode, s.fromStationId, s.toStationId) } }
    }

    private fun StopAreaDto.isInParis() = administrative_regions.any { it.zip_code?.startsWith("75") == true }
    private fun LineDto.toDomain(mode: TransportMode) = MetroLine(id, code ?: name, name, mode, color)
}
