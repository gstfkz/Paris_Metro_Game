package com.gstfkz.parismetrogame.data.repository
import com.gstfkz.parismetrogame.data.model.*
import com.gstfkz.parismetrogame.data.network.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
class MetroRepository(private val api:PrimApiService){
 private var cachedNetwork:MetroNetwork?=null
 suspend fun loadNetwork(forceRefresh:Boolean=false):MetroNetwork{
  cachedNetwork?.let{if(!forceRefresh)return it}
  val metro=api.getLines(PrimApiService.FILTER_METRO).lines.map{it.toDomain(TransportMode.METRO)}
  val rer=api.getLines(PrimApiService.FILTER_RER).lines.map{it.toDomain(TransportMode.RER)}
  val all=metro+rer
  val stations=coroutineScope{all.map{line->async{
   val raw=api.getStopAreasForLine(line.id).stop_areas
   val filtered=if(line.mode==TransportMode.RER)raw.filter{it.isInParis()}else raw
   line.id to filtered.map{MetroStation(it.id,it.name)}.distinctBy{it.id}.sortedBy{it.name}
  }}.associate{it.await()}}
  return MetroNetwork(all,stations).also{cachedNetwork=it}
 }
 fun allStations(n:MetroNetwork)=n.stationsByLine.values.flatten().distinctBy{it.id}
 fun pickRandomStationPair(n:MetroNetwork):Pair<MetroStation,MetroStation>{val p=allStations(n);require(p.size>=2);var a:MetroStation;var b:MetroStation;do{a=p.random();b=p.random()}while(a.id==b.id);return a to b}
 suspend fun fetchOfficialJourneys(from:String,to:String):List<OfficialJourney>{
  val parsed=api.getJourneys(from,to,PrimApiService.FORBIDDEN_MODES,10).journeys.mapNotNull{j->
   val segs=j.sections.mapNotNull{s->
    val fromArea=s.from?.stop_point?.stop_area?:s.from?.stop_area
    val toArea=s.to?.stop_point?.stop_area?:s.to?.stop_area
    val fromId=fromArea?.id?:s.from?.id?:return@mapNotNull null
    val toId=toArea?.id?:s.to?.id?:return@mapNotNull null
    val fromName=fromArea?.name?:s.from?.name?:fromId
    val toName=toArea?.name?:s.to?.name?:toId
    when(s.type){
     "public_transport"->{val info=s.display_informations?:return@mapNotNull null; val pm=info.physical_mode.orEmpty(); val mode=if(pm.contains("RapidTransit",true)||info.commercial_mode.orEmpty().contains("RER",true))TransportMode.RER else TransportMode.METRO; OfficialSegment("",info.code?:info.commercial_mode.orEmpty(),mode,fromId,fromName,toId,toName,s.duration)}
     "street_network","crow_fly"->OfficialSegment("walk","Marche",TransportMode.WALK,fromId,fromName,toId,toName,s.duration)
     else->null
    }
   }
   if(segs.isEmpty())null else OfficialJourney(segs,j.duration)
  }
  val min=parsed.minOfOrNull{it.totalDurationSeconds}?:return emptyList()
  return parsed.filter{it.totalDurationSeconds==min}.distinctBy{it.segments.map{s->listOf(s.mode,s.lineCode,s.fromStationId,s.toStationId)}}
 }
 private fun StopAreaDto.isInParis()=administrative_regions.any{it.zip_code?.startsWith("75")==true}
 private fun LineDto.toDomain(mode:TransportMode)=MetroLine(id,code?:name,name,mode,color)
}
