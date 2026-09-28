package com.gstfkz.parismetrogame.data.network
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
interface PrimApiService {
 @GET("lines") suspend fun getLines(@Query("filter") filter:String,@Query("count") count:Int=100):LinesResponseDto
 @GET("lines/{lineId}/stop_areas") suspend fun getStopAreasForLine(@Path("lineId") lineId:String,@Query("count") count:Int=200):StopAreasResponseDto
 @GET("journeys") suspend fun getJourneys(@Query("from") from:String,@Query("to") to:String,@Query("forbidden_uris[]") forbiddenUris:List<String>,@Query("count") count:Int=10,@Query("max_nb_transfers") maxTransfers:Int=10):JourneysResponseDto
 companion object {
  const val BASE_URL="https://prim.iledefrance-mobilites.fr/marketplace/v2/navitia/"
  const val FILTER_METRO="physical_mode.id=physical_mode:Metro"
  const val FILTER_RER="physical_mode.id=physical_mode:RapidTransit"
  val FORBIDDEN_MODES=listOf("physical_mode:Bus","physical_mode:Tramway","physical_mode:LocalTrain","physical_mode:RailShuttle","physical_mode:Coach","physical_mode:Funicular","physical_mode:Boat","physical_mode:Bicycle","physical_mode:BikeSharingService")
 }
}
