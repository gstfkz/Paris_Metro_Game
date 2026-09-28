package com.gstfkz.parismetrogame.data.model

enum class TransportMode { METRO, RER, WALK }

data class MetroLine(val id:String,val code:String,val name:String,val mode:TransportMode,val colorHex:String?) {
    val displayName:String get() = when(mode){ TransportMode.RER->"RER $code"; TransportMode.WALK->"Marche"; else->"Métro $code" }
    companion object { val WALK = MetroLine("walk","Marche","Marche",TransportMode.WALK,null) }
}
data class MetroStation(val id:String,val name:String)
data class MetroNetwork(val lines:List<MetroLine>,val stationsByLine:Map<String,List<MetroStation>>)
data class UserSegment(val line:MetroLine?=null,val departureStation:MetroStation?=null,val arrivalStation:MetroStation?=null){ val isComplete:Boolean get()=line!=null&&departureStation!=null&&arrivalStation!=null }
data class OfficialSegment(val lineId:String,val lineCode:String,val mode:TransportMode,val fromStationId:String,val fromStationName:String,val toStationId:String,val toStationName:String,val durationSeconds:Int)
data class OfficialJourney(val segments:List<OfficialSegment>,val totalDurationSeconds:Int)
data class ScoreEntry(val score:Int,val timestamp:Long)
sealed class GameScreen {
 data object Menu:GameScreen(); data object Loading:GameScreen()
 data class Playing(val departureStation:MetroStation,val arrivalStation:MetroStation,val segments:List<UserSegment>):GameScreen()
 data class Result(val won:Boolean,val userSegments:List<UserSegment>,val officialJourneys:List<OfficialJourney>):GameScreen()
 data object Scores:GameScreen(); data object Settings:GameScreen()
 data class Error(val message:String):GameScreen()
}
