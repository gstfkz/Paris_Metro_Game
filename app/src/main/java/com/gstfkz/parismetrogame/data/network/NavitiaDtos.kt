package com.gstfkz.parismetrogame.data.network

import kotlinx.serialization.Serializable

// --- Réponse de GET /lines?filter=... ---

@Serializable
data class LinesResponseDto(
    val lines: List<LineDto> = emptyList()
)

@Serializable
data class LineDto(
    val id: String,
    val name: String,
    val code: String? = null,
    val color: String? = null,
    val network: NetworkDto? = null,
    val physical_modes: List<PhysicalModeDto> = emptyList()
)

@Serializable
data class NetworkDto(
    val id: String,
    val name: String
)

@Serializable
data class PhysicalModeDto(
    val id: String,
    val name: String
)

// --- Réponse de GET /lines/{id}/stop_areas ---

@Serializable
data class StopAreasResponseDto(
    val stop_areas: List<StopAreaDto> = emptyList()
)

@Serializable
data class StopAreaDto(
    val id: String,
    val name: String,
    val administrative_regions: List<AdministrativeRegionDto> = emptyList()
)

@Serializable
data class AdministrativeRegionDto(
    val id: String,
    val name: String,
    val zip_code: String? = null,
    val insee: String? = null
)

// --- Réponse de GET /journeys ---

@Serializable
data class JourneysResponseDto(
    val journeys: List<JourneyDto> = emptyList()
)

@Serializable
data class JourneyDto(
    val duration: Int = 0,
    val nb_transfers: Int = 0,
    val tags: List<String> = emptyList(),
    val departure_date_time: String? = null,
    val arrival_date_time: String? = null,
    val requested_date_time: String? = null,
    val sections: List<SectionDto> = emptyList()
)

@Serializable
data class SectionDto(
    val type: String, // "public_transport", "transfer", "street_network", "waiting", "crow_fly"...
    val duration: Int = 0,
    val from: SectionPointDto? = null,
    val to: SectionPointDto? = null,
    val display_informations: DisplayInformationsDto? = null
)

@Serializable
data class SectionPointDto(
    val id: String? = null,
    val name: String? = null,
    val stop_point: StopPointDto? = null,
    val stop_area: StopAreaDto? = null
)

@Serializable
data class StopPointDto(
    val id: String,
    val name: String,
    val stop_area: StopAreaDto? = null
)

@Serializable
data class DisplayInformationsDto(
    val commercial_mode: String? = null,
    val physical_mode: String? = null,
    val code: String? = null, // numéro/lettre de ligne affiché (ex: "1", "A")
    val color: String? = null,
    val network: String? = null
)
