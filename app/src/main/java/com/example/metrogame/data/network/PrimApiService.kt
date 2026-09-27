package com.example.metrogame.data.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API PRIM (Île-de-France Mobilités), moteur Navitia, périmètre "fr-idf".
 * Base URL : https://prim.iledefrance-mobilites.fr/marketplace/navitia/coverage/fr-idf/
 * Authentification : header "apikey" (voir PrimApiClient), PAS de Bearer/OAuth.
 *
 * NB : ces signatures sont établies à partir de la documentation Navitia générale ;
 * n'ayant pas d'accès réseau pour tester en direct, vérifie le premier appel réel
 * (ex. avec un client HTTP) avant de lancer l'intégration complète de l'UI.
 */
interface PrimApiService {

    /**
     * Récupère les lignes correspondant à un mode physique donné.
     * [physicalModeId] : "physical_mode:Metro" ou "physical_mode:RapidTransit" (RER).
     */
    @GET("lines")
    suspend fun getLines(
        @Query("filter") filter: String,
        @Query("count") count: Int = 100
    ): LinesResponseDto

    /** Stations desservies par une ligne donnée. */
    @GET("lines/{lineId}/stop_areas")
    suspend fun getStopAreasForLine(
        @Path("lineId") lineId: String,
        @Query("count") count: Int = 200
    ): StopAreasResponseDto

    /**
     * Calcule l'itinéraire le plus rapide en temps entre deux stop_areas, en excluant
     * tous les modes hors métro/RER via des `forbidden_uris[]` répétés.
     */
    @GET("journeys")
    suspend fun getJourneys(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("forbidden_uris[]") forbiddenUris: List<String>,
        @Query("count") count: Int = 1,
        @Query("max_nb_transfers") maxTransfers: Int = 10
    ): JourneysResponseDto

    companion object {
        const val BASE_URL = "https://prim.iledefrance-mobilites.fr/marketplace/navitia/coverage/fr-idf/"

        const val FILTER_METRO = "physical_mode.id=physical_mode:Metro"
        const val FILTER_RER = "physical_mode.id=physical_mode:RapidTransit"

        /** Modes à exclure pour ne garder que métro + RER dans l'itinéraire officiel. */
        val FORBIDDEN_MODES = listOf(
            "physical_mode:Bus",
            "physical_mode:Tramway",
            "physical_mode:LocalTrain", // Transilien
            "physical_mode:RailShuttle",
            "physical_mode:Coach",
            "physical_mode:Funicular",
            "physical_mode:Boat",
            "physical_mode:Bicycle",
            "physical_mode:BikeSharingService"
        )
    }
}
