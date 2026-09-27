package com.example.metrogame.util

import com.example.metrogame.data.model.OfficialJourney
import com.example.metrogame.data.model.UserSegment

/**
 * Compare la suite de tronçons saisis par l'utilisateur à l'itinéraire officiel.
 *
 * Règle retenue (à ajuster si besoin) : il faut la MÊME séquence exacte de
 * (ligne, station de départ, station d'arrivée) — un itinéraire de même durée mais
 * emprutant une combinaison différente de lignes n'est pas compté comme gagnant.
 * S'il existe plusieurs itinéraires ex-aequo au même temps, seul celui renvoyé en
 * premier par l'API est comparé (limitation actuelle, cf. README).
 */
object ItineraryComparator {

    fun matches(userSegments: List<UserSegment>, official: OfficialJourney): Boolean {
        val complete = userSegments.filter { it.isComplete }
        if (complete.size != official.segments.size) return false

        return complete.indices.all { i ->
            val user = complete[i]
            val ref = official.segments[i]
            val line = user.line ?: return@all false
            val departure = user.departureStation ?: return@all false
            val arrival = user.arrivalStation ?: return@all false

            line.mode == ref.mode &&
                line.code.equals(ref.lineCode, ignoreCase = true) &&
                departure.id == ref.fromStationId &&
                arrival.id == ref.toStationId
        }
    }
}
