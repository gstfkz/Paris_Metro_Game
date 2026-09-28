package com.gstfkz.parismetrogame.util
import com.gstfkz.parismetrogame.data.model.*
object ItineraryComparator{
 fun matches(user:List<UserSegment>,officials:List<OfficialJourney>)=officials.any{matchesOne(user,it)}
 private fun matchesOne(user:List<UserSegment>,official:OfficialJourney):Boolean{
  val u=user.filter{it.isComplete}; if(u.size!=official.segments.size)return false
  return u.indices.all{i->val a=u[i];val b=official.segments[i]; val line=a.line?:return@all false; val dep=a.departureStation?:return@all false; val arr=a.arrivalStation?:return@all false; line.mode==b.mode&&(line.mode==TransportMode.WALK||line.code.equals(b.lineCode,true))&&dep.id==b.fromStationId&&arr.id==b.toStationId}
 }
}
